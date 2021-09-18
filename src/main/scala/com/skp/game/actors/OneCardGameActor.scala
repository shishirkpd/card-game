package com.skp.game.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.skp.game.model.{PLAYING, User}
import com.skp.game.service.UserService

object OneCardGameActor {
  var listOfUsers = List.empty[User]
  var listOfGame: Map[String, ActorRef[Command]] = Map[String, ActorRef[Command]]()
  def initialise(userService: UserService): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case StartGame(user) => listOfUsers = listOfUsers :+ user
        if(listOfUsers.size >= 2) {
          val usersForGame = listOfUsers.take(2)
          usersForGame.foreach(u => userService.updateStatus(u.copy(status = PLAYING)))
          listOfUsers = listOfUsers.filterNot(x => usersForGame.contains(x))
          val inProgressGameActor: ActorRef[Command] = context.spawn(InProgressGameActor(userService), s"InProgressGameFor${usersForGame.head.name}-${usersForGame(1).name}")
          listOfGame += ((usersForGame.head.name, inProgressGameActor))
          listOfGame += ((usersForGame(1).name, inProgressGameActor))
          inProgressGameActor ! BeginGame(usersForGame.head, usersForGame(1))
        }
        Behaviors.same
      case FoldGame(user) =>
        listOfGame.get(user.name)  match {
          case Some(actorRef) =>  actorRef ! FoldGame(user)
        }
        Behaviors.same
      case _  => Behaviors.same
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)
}
