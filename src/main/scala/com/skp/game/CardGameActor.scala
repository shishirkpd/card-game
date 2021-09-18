package com.skp.game

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.skp.game.model.{ActionPerformed, User, UserResponse}
import com.skp.game.service.UserService

object CardGameActor {
  sealed trait Command
  final case class CreateUser(user: User,  replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[UserResponse]) extends Command

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
      case _ => Behaviors.same
    }
  }

  def apply(userService: UserService): Behavior[Command] = supervisor(userService)


}
