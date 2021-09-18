package com.skp.game.model


object  PlayingCard {
  sealed trait Suit

  object Suit {
    case object Diamond extends Suit

    case object Spade extends Suit

    case object Club extends Suit

    case object Heart extends Suit

    val all = List(Diamond, Spade, Club, Heart)
  }

  sealed trait Card

  case class NumberCard(number: String, suit: Suit) extends Card

  def faceCard(str: String): List[Card] = (for {
    suit <- Suit.all
  } yield NumberCard(str, suit))

  val deck: List[Card] = faceCard("A") ::: faceCard("K") :::  faceCard("Q") :::
    faceCard("J") :::(for {
    suit <- Suit.all
    number <- 2 to 10
  } yield NumberCard(number.toString, suit))
}