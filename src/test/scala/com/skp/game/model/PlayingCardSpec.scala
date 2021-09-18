package com.skp.game.model

import com.skp.game.model.PlayingCard.NumberCard
import com.skp.game.model.PlayingCard.Suit.Diamond
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayingCardSpec extends AnyWordSpec  with Matchers {

  "PlayingCardSpec" should {
    "isBigger return true" in {
      val card1 = NumberCard("A", Diamond)
      val card2 = NumberCard("7", Diamond)
      PlayingCard.isBigger(card1, card2) shouldBe true
    }

    "isBigger return false" in {
      val card1 = NumberCard("10", Diamond)
      val card2 = NumberCard("K", Diamond)
      PlayingCard.isBigger(card1, card2) shouldBe false
    }
  }
}
