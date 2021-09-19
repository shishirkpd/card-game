package com.skp.game.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors._
import com.skp.game.model.PlayingCard.{NumberCard, isBigger}
import com.skp.game.model.{ActionPerformed, LOBBY, Player, PlayingCard, User}
import com.skp.game.service.UserService
import org.slf4j.LoggerFactory

import scala.util.Random

object InProgressGameActor {
  var players: List[Player] = List[Player]()
  var isShow = false

  private val logger = LoggerFactory.getLogger(getClass)

  def checkCards(players: List[Player]): Player = {
    val player1Cards = players.head.card
    val player2Cards = players(1).card

    val rs: Seq[List[Boolean]] = player1Cards.map { card =>
      player2Cards.map(isBigger(card, _))
    }

    rs.collect {
      case x :: tail if x == true & tail.forall(_ == true) => players.head
      case _ => players(1)
    }.head

  }

  def checkEqualCards(players: List[Player]): Boolean = {
    val player1Cards = players.head.card.map(_.number)
    val player2Cards = players(1).card.map(_.number)
    player1Cards.diff(player2Cards).size == 0
  }

  def initialise(userService: UserService): Behavior[Command] = {
    receiveMessage {
      case BeginGame(player1, player2) =>
        val player1Card: NumberCard = PlayingCard.deck(Random.nextInt(52))
        val player2Card: NumberCard = PlayingCard.deck.filterNot(_ == player1Card)(Random.nextInt(51))

        players = players :+ Player(player1, 3, List(player1Card))
        players = players :+ Player(player2, 3, List(player2Card))

        logger.info(s"Starting game for users: ${player1.name} with card: $player1Card " +
          s"and ${player2.name} with card: $player2Card")
        same

      case FoldInProgressGameForUser(user) =>
        logger.info(s"Game folded by user: ${user.name}")
        val looser: Player = players.filter(_.user == user).head
        val winner: Player = players.filterNot(_.user == user).head

        userService.updateStatus(User(winner.user.name, winner.user.tokens + looser.token, LOBBY))
        userService.updateStatus(User(looser.user.name, looser.user.tokens - looser.token, LOBBY))
        stopped

      case ShowInProgressGameForUser(_, replyTo) =>
        if(isShow) {
          if(checkEqualCards(players)) {
            replyTo ! ActionPerformed(s"${userService.findBy(players.head.user.name).head.name} and ${userService.findBy(players(1).user.name).head.name} wins the game ..!!")
          } else {
            val losingPlayer = checkCards(players)
            val looser: Player = players.filter(_.user == losingPlayer.user).head
            val winner: Player = players.filterNot(_.user == losingPlayer.user).head

            userService.updateStatus(User(winner.user.name, winner.user.tokens + looser.token, LOBBY))
            userService.updateStatus(User(looser.user.name, looser.user.tokens - looser.token, LOBBY))
            replyTo ! ActionPerformed(s"${userService.findBy(winner.user.name).head.name} wins the game ..!!")
          }
          stopped
        } else {
          isShow = true
          replyTo ! ActionPerformed(s"waiting for other user show call ..!!")
          same
        }
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)

}
