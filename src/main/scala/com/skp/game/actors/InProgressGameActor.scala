package com.skp.game.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors._
import com.skp.game.model.GameType.{OneCard, TwoCard}
import com.skp.game.model.PlayingCard.{NumberCard, isBigger}
import com.skp.game.model._
import com.skp.game.service.UserService
import org.slf4j.LoggerFactory

import scala.util.Random

object InProgressGameActor {
  private val logger = LoggerFactory.getLogger(getClass)

  def checkCards(players: List[Player]): Player = {
    val player1Cards = players.head.card
    val player2Cards = players(1).card

    val rs: Seq[List[Boolean]] = player1Cards.map { card =>
      player2Cards.map(isBigger(card, _))
    }

    rs.map(x => x.forall(_ == true)).forall(_ == false) match {
      case true => players(1)
      case false => players.head
    }
  }

  def checkEqualCards(players: List[Player]): Boolean = {
    val player1Cards = players.head.card.map(_.number)
    val player2Cards = players(1).card.map(_.number)
    player1Cards.diff(player2Cards).size == 0
  }

  def initialise(userService: UserService): Behavior[Command] = setup { context =>
    var players: List[Player] = List[Player]()
    var isShow = false
    var isFold = false
    receiveMessage {
      case BeginGame(player1, player2, gameType) =>
        gameType match {
        case OneCard =>
          val player1Card: NumberCard = PlayingCard.deck(Random.nextInt(52))
          val player2Card: NumberCard = PlayingCard.deck.filterNot(_ == player1Card)(Random.nextInt(51))

          players = players :+ Player(player1, List(player1Card))
          players = players :+ Player(player2, List(player2Card))

          logger.info(s"Starting game for users: ${player1.name} with card: $player1Card " +
            s"and ${player2.name} with card: $player2Card")
        case TwoCard =>
          val player1Card1: NumberCard = PlayingCard.deck(Random.nextInt(52))
          val player1Card2: NumberCard = PlayingCard.deck.filterNot(_ == player1Card1)(Random.nextInt(51))
          val player2Card1: NumberCard = PlayingCard.deck.filterNot(x => x == player1Card1 || x == player1Card2)(Random.nextInt(50))
          val player2Card2: NumberCard = PlayingCard.deck.filterNot(x => x == player1Card1 || x == player1Card2 || x == player2Card1)(Random.nextInt(49))

          players = players :+ Player(player1, List(player1Card1, player1Card2))
          players = players :+ Player(player2, List(player2Card1, player2Card2))

          logger.info(s"Starting game for users: ${player1.name} with card: $player1Card1 and $player1Card2 " +
            s"and ${player2.name} with card: $player2Card1 and $player2Card2")
        }
        same

      case FoldInProgressGameForUser(user, replyTo, gameType) => {
        (isShow, isFold) match {
          case (true, false) =>
            logger.info(s"Game folded by user: ${user.name}")
            val player1: Player = players.filter(_.user == user).head
            val player2: Player = players.filterNot(_.user == user).head

            val tokensForPlayer1 = (gameType, isShow) match {
              case (OneCard, true) => 3
              case (OneCard, false) => 1
              case (TwoCard, true) => 5
              case (TwoCard, false) => 2
            }

            val tokensForPlayer2 = (gameType, isShow) match {
              case (OneCard, true) => 3
              case (OneCard, false) => -1
              case (TwoCard, true) => 5
              case (TwoCard, false) => -2
            }

            userService.updateStatus(User(player1.user.name, player1.user.tokens - tokensForPlayer1, LOBBY))
            userService.updateStatus(User(player2.user.name, player2.user.tokens + tokensForPlayer2, LOBBY))
            isShow = false
            isFold = false
            replyTo ! ActionPerformed(s"Game folded by user: ${user.name}")
            stopped
          case (false, true) =>
            logger.info(s"Game folded by user: ${user.name}")
            val player1: Player = players.filter(_.user == user).head
            val player2: Player = players.filterNot(_.user == user).head
            val tokensForPlayer = gameType match {
              case OneCard => 1
              case TwoCard => 2
            }
            userService.updateStatus(User(player1.user.name, player1.user.tokens - tokensForPlayer, LOBBY))
            userService.updateStatus(User(player2.user.name, player2.user.tokens - tokensForPlayer, LOBBY))
            isShow = false
            isFold = false
            replyTo ! ActionPerformed(s"Game folded by user: ${user.name}")
            stopped
          case (false, false) =>
            isFold = true
            replyTo ! ActionPerformed(s"Game folded by user: ${user.name}, waiting for other user")
            same
        }
      }
      case ShowInProgressGameForUser(user, replyTo, gameType) => {
        (isShow, isFold) match {
          case (true, false) =>
            if (checkEqualCards(players)) {
            players.map(_.user).foreach(u => userService.updateStatus(User(u.name, u.tokens, LOBBY)))
            replyTo ! ActionPerformed(s"${userService.findBy(players.head.user.name).head.name} and ${userService.findBy(players(1).user.name).head.name} wins the game ..!!")
          } else {
            logger.debug(s"Player list $players")
            val winner: Player = checkCards(players)
            val looser: Player = players.filterNot(_.user == winner.user).head
            val tokensForUser = gameType match {
              case OneCard => 10
              case TwoCard => 20
            }

            userService.updateStatus(User(winner.user.name, winner.user.tokens + tokensForUser, LOBBY))
            userService.updateStatus(User(looser.user.name, looser.user.tokens - tokensForUser, LOBBY))
            isShow = false
            isFold = false
            replyTo ! ActionPerformed(s"${userService.findBy(winner.user.name).head.name} wins the game with ${winner.card}  vs ${looser.card}..!!")
          }
            stopped
          case (false, true) => {
            logger.info(s"Game show by user: ${user.name}")
            val player1: Player = players.filter(_.user == user).head
            val player2: Player = players.filterNot(_.user == user).head

            val tokensForPlayer = gameType match {
              case OneCard => 3
              case TwoCard => 5
            }

            userService.updateStatus(User(player1.user.name, player1.user.tokens + tokensForPlayer, LOBBY))
            userService.updateStatus(User(player2.user.name, player2.user.tokens - tokensForPlayer, LOBBY))

            val message = s"${userService.findBy(player1.user.name).head.name} wins the game other player folded..!!"
            logger.info(message)

            replyTo ! ActionPerformed(message)
            isShow = false
            isFold = false
          }
            stopped
          case (false, false) => {
            isShow = true
            replyTo ! ActionPerformed(s"waiting for other user show call ..!!")
            same
          }
        }
      }
    }
  }

  def apply(userService: UserService): Behavior[Command] = initialise(userService)

}
