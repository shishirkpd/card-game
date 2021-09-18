package com.skp.game.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.skp.game.model._
import com.skp.game.service.UserService

object CardGameActor {

  def supervisor(userService: UserService, oneCardGame: ActorRef[Command]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case CreateUser(user, replyTo) =>
        userService.create(user) match {
          case user: User => replyTo ! ActionPerformed(s"User create with details: " + user.toString)
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
            oneCardGame ! StartGame(updatedUser)
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

  def apply(userService: UserService, actorRef: ActorRef[Command]): Behavior[Command] = supervisor(userService, actorRef)


}
