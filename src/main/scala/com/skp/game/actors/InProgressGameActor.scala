package com.skp.game.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.skp.game.model.PlayingCard.Card
import com.skp.game.model.{ActionPerformed, LOBBY, PlayingCard, User}
import com.skp.game.service.UserService
import scala.util.Random

object InProgressGameActor {
  case class Player(user: User, token: Int, card: List[Card])
  var players: List[Player] = List[Player]()
  var isShow = false

  def checkCards(players: List[Player]) = {

  }

  def initialise(userService: UserService): Behavior[Command] = {
    Behaviors.receiveMessage {
      case BeginGame(player1, player2) =>
        val player1Card = List(PlayingCard.deck(Random.nextInt(52)))
        val player2Card = List(PlayingCard.deck.filterNot(_ == player1Card)(Random.nextInt(51)))

        players = players :+ Player(player1, 3, player1Card)
        players = players :+ Player(player2, 3, player2Card)

        Behaviors.same

      case FoldGame(user) =>
        val looser: Player = players.filter(_.user == user)(0)
        val winner: Player = players.filterNot(_.user == user)(0)

        userService.updateStatus(User(winner.user.name, winner.token + looser.token, LOBBY))
        userService.updateStatus(User(looser.user.name, looser.token - looser.token, LOBBY))
        Behaviors.stopped

      case Show(_, replyTo) =>
        if(isShow) {
          val losingPlayer = checkCards(players)
          val looser: Player = players.filter(_.user == losingPlayer)(0)
          val winner: Player = players.filterNot(_.user == losingPlayer)(0)

          userService.updateStatus(User(winner.user.name, winner.token + looser.token, LOBBY))
          userService.updateStatus(User(looser.user.name, looser.token - looser.token, LOBBY))
          replyTo ! ActionPerformed(s"${userService.findBy(winner.user.name)} wins the game ..!!")
          Behaviors.stopped
        } else {
          isShow = true
          Behaviors.same
        }
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)

}
