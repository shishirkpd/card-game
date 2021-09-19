package com.skp.game.actors

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import com.skp.game.model.{PLAYING, User}
import com.skp.game.service.UserService
import org.slf4j.LoggerFactory

object CardGameActor {
  var listOfUsers = List.empty[User]
  var listOfGame: Map[String, ActorRef[Command]] = Map[String, ActorRef[Command]]()
  private val logger = LoggerFactory.getLogger(getClass)

  def initialise(userService: UserService): Behavior[Command] = setup { context =>
    receiveMessage {
      case StartGame(user) => listOfUsers = listOfUsers :+ user
        if(listOfUsers.size >= 2) {
          val usersForGame = listOfUsers.take(2)
          val updateUser = usersForGame.map(_.copy(status = PLAYING))
          updateUser.foreach(u => userService.updateStatus(u.copy(status = PLAYING)))

          listOfUsers = listOfUsers.filterNot(x => updateUser.contains(x))

          val inProgressGameActor: ActorRef[Command] = context.spawn(InProgressGameActor(userService),
            s"""InProgressGameFor-${updateUser.head.name}-${updateUser(1).name}""")
          logger.info(s"New game actor started ${inProgressGameActor.ref}")

          listOfGame += ((updateUser.head.name, inProgressGameActor))
          listOfGame += ((updateUser(1).name, inProgressGameActor))

          inProgressGameActor ! BeginGame(updateUser.head, updateUser(1))
        }
        same
      case FoldForUser(user) =>
        listOfGame.get(user.name) match {
            case Some(actorRef) =>  actorRef ! FoldInProgressGameForUser(user)
            logger.info(s"${user.name} folded ..!!")
          }
        same
      case ShowForUser(user, replyTo) =>
        listOfGame.get(user.name) match {
            case Some(actorRef) =>  actorRef ! ShowInProgressGameForUser(user, replyTo)
            logger.info(s"${user.name} call show ..!!")
          }
        same

      case _  => same
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)
}
