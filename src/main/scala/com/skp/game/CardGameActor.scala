package com.skp.game

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.skp.game.model.{ActionPerformed, LOBBY, PLAYING, User, UserResponse, WAITING}
import com.skp.game.service.UserService

object CardGameActor {
  sealed trait Command
  final case class CreateUser(user: User,  replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[UserResponse]) extends Command
  final case class Play(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  def supervisor(userService: UserService): Behavior[Command]= {
    Behaviors.receiveMessage {
      case CreateUser(user, replyTo) =>
        userService.create(user) match {
          case user: User =>  replyTo ! ActionPerformed(s"User create with details: " + user.toString)
            Behaviors.same
          case _ => replyTo ! ActionPerformed(s"User can not be created")
            Behaviors.same
        }
      case GetUser(name, replyTo) =>
        replyTo ! UserResponse(userService.findBy(name))
        Behaviors.same
      case Play(name, replyTo) =>
        userService.findBy(name) match {
          case Some(user) if user.status == LOBBY =>
            val updatedUser = User(user.name, user.tokens, WAITING)
            userService.updateStatus(updatedUser)
            replyTo ! ActionPerformed(s"Waiting for opponent to join")
            Behaviors.same
          case Some(user) if user.status == WAITING =>
            replyTo ! ActionPerformed(s"Waiting for opponent to join")
            Behaviors.same
          case Some(user) if user.status == PLAYING =>
            replyTo ! ActionPerformed(s"Game in progress")
            Behaviors.same
          case _ => replyTo ! ActionPerformed(s"Action could not be performed")
            Behaviors.same
        }
      case _ => Behaviors.same
    }
  }

  def apply(userService: UserService): Behavior[Command] = supervisor(userService)


}
