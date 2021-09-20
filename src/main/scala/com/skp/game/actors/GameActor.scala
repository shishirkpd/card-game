package com.skp.game.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors._
import com.skp.game.model._
import com.skp.game.service.UserService

object GameActor {

  def supervisor(userService: UserService): Behavior[Command] = {
    setup { context =>
      val cardGame = context.spawn(CardGameActor(userService), "CardGameActor")
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
              userService.updateStatus(updatedUser)
              cardGame ! StartGame(updatedUser, gameType)
              replyTo ! ActionPerformed(s"Waiting for opponent to join")
              same
            case Some(user) if user.status == WAITING =>
              replyTo ! ActionPerformed(s"Waiting for opponent to join")
              same
            case Some(user) if user.status == PLAYING =>
              replyTo ! ActionPerformed(s"Game in progress")
              same
            case None => replyTo ! ActionPerformed(s"Action could not be performed as $name not register")
              same
          }
        case FoldGame(name, replyTo, gameType) =>
          userService.findBy(name) match {
            case Some(user) =>  cardGame ! FoldForUser(user, replyTo, gameType)
            case None => replyTo ! ActionPerformed(s"Action could not be performed as $name not register")
          }
          same
        case Show(name, replyTo, gameType) =>
          userService.findBy(name) match {
            case Some(user) =>  cardGame ! ShowForUser(user, replyTo, gameType)
            case None => replyTo ! ActionPerformed(s"Action could not be performed as $name not register")
          }
          same
        case _ => same
      }
    }
  }

  def apply(userService: UserService): Behavior[Command] = supervisor(userService)


}
