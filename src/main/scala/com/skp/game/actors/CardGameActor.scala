package com.skp.game.actors

import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.{ActorRef, Behavior}
import com.skp.game.model.GameType.{OneCard, TwoCard}
import com.skp.game.model.{ActionPerformed, GameType, PLAYING, User}
import com.skp.game.service.UserService
import org.slf4j.LoggerFactory

object CardGameActor {
  var listOfUsers = List.empty[(User, GameType.Value)]
  var listOfGame: Map[String, ActorRef[Command]] = Map[String, ActorRef[Command]]()
  private val logger = LoggerFactory.getLogger(getClass)

  def initialise(userService: UserService): Behavior[Command] = setup { context =>
    receiveMessage {
      case StartGame(user, gameType) => listOfUsers = listOfUsers :+ (user, gameType)
        gameType match {
          case OneCard =>
            if (listOfUsers.count(_._2 == gameType) >= 2) {
              val usersForGame = listOfUsers.filter(_._2 == gameType).map(_._1).take(2)
              val updateUser = usersForGame.map(_.copy(status = PLAYING))
              updateUser.foreach(u => userService.updateStatus(u.copy(status = PLAYING)))

              listOfUsers = listOfUsers.filterNot(x => updateUser.map(_.name).contains(x._1.name))

              val inProgressGameActor: ActorRef[Command] = context.spawn(InProgressGameActor(userService),
                s"""InProgressGameFor-${updateUser.head.name}-${updateUser(1).name}""")
              logger.info(s"New game actor started ${inProgressGameActor.ref} $gameType")

              listOfGame += ((updateUser.head.name, inProgressGameActor))
              listOfGame += ((updateUser(1).name, inProgressGameActor))

              inProgressGameActor ! BeginGame(updateUser.head, updateUser(1), gameType)
            }
          case TwoCard => {
            if (listOfUsers.count(_._2 == gameType) >= 2) {
              val usersForGame = listOfUsers.filter(_._2 == gameType).map(_._1).take(2)
              val updateUser = usersForGame.map(_.copy(status = PLAYING))
              updateUser.foreach(u => userService.updateStatus(u.copy(status = PLAYING)))

              listOfUsers = listOfUsers.filterNot(x => updateUser.map(_.name).contains(x._1.name))

              val inProgressGameActor: ActorRef[Command] = context.spawn(InProgressGameActor(userService),
                s"""InProgressGameFor-${updateUser.head.name}-${updateUser(1).name}""")
              logger.info(s"New game actor started ${inProgressGameActor.ref} $gameType")

              listOfGame += ((updateUser.head.name, inProgressGameActor))
              listOfGame += ((updateUser(1).name, inProgressGameActor))

              inProgressGameActor ! BeginGame(updateUser.head, updateUser(1), gameType)
            }
          }
        }
        same
      case FoldForUser(user, replyTo, gameType) =>
        listOfGame.get(user.name) match {
            case Some(actorRef) =>  actorRef ! FoldInProgressGameForUser(user, replyTo, gameType)
            logger.info(s"${user.name} folded ..!!")
            case None => replyTo ! ActionPerformed(s"Sorry no game is progress..!!")
          }
        same
      case ShowForUser(user, replyTo, gameType) =>
        listOfGame.get(user.name) match {
            case Some(actorRef) =>  actorRef ! ShowInProgressGameForUser(user, replyTo, gameType)
            logger.info(s"${user.name} call show ..!!")
            case None => replyTo ! ActionPerformed(s"Sorry no game is progress..!!")
          }
        same

      case _  => same
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)
}
