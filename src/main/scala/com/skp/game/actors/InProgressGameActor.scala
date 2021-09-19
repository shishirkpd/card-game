package com.skp.game.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.skp.game.model.PlayingCard.isBigger
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
    val result: Seq[Boolean] = for {
      c1 <- player1Cards
      c2 <- player2Cards
    } yield {
      isBigger(c1, c2)
    }
    if (result.forall(_ == false)) players.head else players(1)
  }

  def initialise(userService: UserService): Behavior[Command] = {
    Behaviors.receiveMessage {
      case BeginGame(player1, player2) =>
        val player1Card = PlayingCard.deck(Random.nextInt(52))
        val player2Card = PlayingCard.deck.filterNot(_ == player1Card)(Random.nextInt(51))

        players = players :+ Player(player1, 3, List(player1Card))
        players = players :+ Player(player2, 3, List(player2Card))

        logger.info(s"Starting game for users: ${player1.name} with card: ${player1Card} " +
          s"and ${player2.name} with card: ${player2Card}")
        Behaviors.same

      case FoldInProgressGameForUser(user) =>
        logger.info(s"Game folded by user: ${user.name}")
        val looser: Player = players.filter(_.user == user).head
        val winner: Player = players.filterNot(_.user == user).head

        userService.updateStatus(User(winner.user.name, winner.user.tokens + looser.token, LOBBY))
        userService.updateStatus(User(looser.user.name, looser.user.tokens - looser.token, LOBBY))
        Behaviors.stopped

      case Show(_, replyTo) =>
        if(isShow) {
          val losingPlayer = checkCards(players)
          val looser: Player = players.filter(_.user == losingPlayer.user).head
          val winner: Player = players.filterNot(_.user == losingPlayer.user).head

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
