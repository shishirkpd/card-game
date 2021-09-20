package com.skp.game.actors

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import com.skp.game.model._
import com.skp.game.service.UserService

object GameActor {

  def supervisor(userService: UserService, oneCardGame: ActorRef[Command]): Behavior[Command] = {
    receiveMessage {
      case CreateUser(user, replyTo) =>
        userService.create(user) match {
          case user: User => replyTo ! ActionPerformed(s"User create with details: " + user.toString)
            same
          case _ => replyTo ! ActionPerformed(s"User can not be created")
            same
        }
      case GetUser(name, replyTo) =>
        replyTo ! UserResponse(userService.findBy(name))
        same
      case Play(name, replyTo, gameType) =>
        userService.findBy(name) match {
          case Some(user) if user.status == LOBBY =>
            val updatedUser = User(user.name, user.tokens, WAITING)
            oneCardGame ! StartGame(updatedUser, gameType)
            userService.updateStatus(updatedUser)
            replyTo ! ActionPerformed(s"Waiting for opponent to join")
            same
          case Some(user) if user.status == WAITING =>
            replyTo ! ActionPerformed(s"Waiting for opponent to join")
            same
          case Some(user) if user.status == PLAYING =>
            replyTo ! ActionPerformed(s"Game in progress")
            same
          case _ => replyTo ! ActionPerformed(s"Action could not be performed")
            same
        }
      case FoldGame(name, replyTo, gameType) =>
        userService.findBy(name) match {
          case Some(user) => oneCardGame ! FoldForUser(user, replyTo, gameType)

        }
        same
      case Show(name, replyTo, gameType) =>
        userService.findBy(name) match {
          case Some(user) => oneCardGame ! ShowForUser(user, replyTo, gameType)
        }
        same
      case _ => same
    }
  }

  def apply(userService: UserService, actorRef: ActorRef[Command]): Behavior[Command] = supervisor(userService, actorRef)


}
