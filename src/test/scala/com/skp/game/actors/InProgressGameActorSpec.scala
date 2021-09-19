package com.skp.game.actors

import com.skp.game.model.PlayingCard.NumberCard
import com.skp.game.model.PlayingCard.Suit.{Club, Diamond, Heart, Spade}
import com.skp.game.model.{Player, User}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InProgressGameActorSpec extends AnyWordSpec with Matchers {

  "InProgressGameActor" should {
    "checkEqualCards" should {
      "it should return true for Player1 and Player2 card rank are same" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards = NumberCard("J", Diamond)
        val player2Cards = NumberCard("J", Heart)
        val player1 = Player(user1, 3, List(player1Cards))
        val player2 = Player(user2, 3, List(player2Cards))

        InProgressGameActor.checkEqualCards(List(player1, player2)) shouldBe true
      }

      "it return false for Player1 and Player2 card rank are different" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards = NumberCard("J", Diamond)
        val player2Cards = NumberCard("2", Heart)
        val player1 = Player(user1, 3, List(player1Cards))
        val player2 = Player(user2, 3, List(player2Cards))

        InProgressGameActor.checkEqualCards(List(player1, player2)) shouldBe false
      }

      "it should return true for Player1 and Player2 card rank are same for multiple card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards1 = NumberCard("J", Diamond)
        val player1Cards2 = NumberCard("9", Diamond)
        val player2Cards2 = NumberCard("J", Heart)
        val player2Cards1 = NumberCard("9", Spade)
        val player1 = Player(user1, 3, List(player1Cards1, player1Cards2))
        val player2 = Player(user2, 3, List(player2Cards1, player2Cards2))

        InProgressGameActor.checkEqualCards(List(player1, player2)) shouldBe true
      }

      "it return false for Player1 and Player2 card rank are different for multiple card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards1 = NumberCard("J", Diamond)
        val player1Cards2 = NumberCard("9", Diamond)
        val player2Cards2 = NumberCard("J", Heart)
        val player2Cards1 = NumberCard("5", Spade)
        val player1 = Player(user1, 3, List(player1Cards1, player1Cards2))
        val player2 = Player(user2, 3, List(player2Cards1, player2Cards2))

        InProgressGameActor.checkEqualCards(List(player1, player2)) shouldBe false
      }
    }

    "checkCards" should {
      "it should return Player1 for Player1 and Player2 as Player1 has high card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards = NumberCard("J", Diamond)
        val player2Cards = NumberCard("4", Heart)
        val player1 = Player(user1, 3, List(player1Cards))
        val player2 = Player(user2, 3, List(player2Cards))

        InProgressGameActor.checkCards(List(player1, player2)) shouldBe player1
      }

      "it return Player2 for Player1 and Player2 as Player2 has high card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards = NumberCard("J", Diamond)
        val player2Cards = NumberCard("A", Club)
        val player1 = Player(user1, 3, List(player1Cards))
        val player2 = Player(user2, 3, List(player2Cards))

        InProgressGameActor.checkCards(List(player1, player2)) shouldBe player2
      }

      "it should return Player1 for Player1 and Player2 card higher for Player1 for multiple card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards1 = NumberCard("J", Diamond)
        val player1Cards2 = NumberCard("9", Diamond)
        val player2Cards2 = NumberCard("J", Heart)
        val player2Cards1 = NumberCard("7", Spade)
        val player1 = Player(user1, 3, List(player1Cards1, player1Cards2))
        val player2 = Player(user2, 3, List(player2Cards1, player2Cards2))

        InProgressGameActor.checkCards(List(player1, player2)) shouldBe player1
      }

      "it should return Player2 for Player1 and Player2 card higher for Player2 for multiple card" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards1 = NumberCard("J", Diamond)
        val player1Cards2 = NumberCard("9", Diamond)
        val player2Cards2 = NumberCard("J", Heart)
        val player2Cards1 = NumberCard("Q", Spade)
        val player1 = Player(user1, 3, List(player1Cards1, player1Cards2))
        val player2 = Player(user2, 3, List(player2Cards1, player2Cards2))

        InProgressGameActor.checkCards(List(player1, player2)) shouldBe player2
      }

      "return Player2 wins the game with List(NumberCard(K,Spade), NumberCard(2,Diamond))  vs List(NumberCard(5,Spade), NumberCard(K,Diamond))" in {
        val user1 = User("Player1")
        val user2 = User("Player2")
        val player1Cards1 = NumberCard("5", Spade)
        val player1Cards2 = NumberCard("K", Diamond)
        val player2Cards2 = NumberCard("K", Spade)
        val player2Cards1 = NumberCard("2", Diamond)
        val player1 = Player(user1, 3, List(player1Cards1, player1Cards2))
        val player2 = Player(user2, 3, List(player2Cards1, player2Cards2))

        InProgressGameActor.checkCards(List(player1, player2)) shouldBe player1
      }
    }
  }
}
