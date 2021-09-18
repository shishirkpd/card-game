package com.skp.game.model


object PlayingCard {
  sealed trait Suit

  object Suit {
    case object Diamond extends Suit

    case object Spade extends Suit

    case object Club extends Suit

    case object Heart extends Suit

    val all = List(Diamond, Spade, Club, Heart)
  }

  case class NumberCard(number: String, suit: Suit)

  def faceCard(str: String): List[NumberCard] = for {
    suit <- Suit.all
  } yield NumberCard(str, suit)

  val deck: List[NumberCard] = faceCard("A") ::: faceCard("K") :::  faceCard("Q") :::
    faceCard("J") :::(for {
    suit <- Suit.all
    number <- 2 to 10
  } yield NumberCard(number.toString, suit))

  def isBigger(c1: NumberCard, c2: NumberCard): Boolean = (c1.number, c2.number) match {
    case ("A", _) => true
    case ("K", "A") => false
    case ("K", _) => true
    case ("Q", "A" | "K") => false
    case ("Q", _) => true
    case ("J", "A" | "K" | "Q") => false
    case ("J", _) => true
    case (_, "A" | "K" | "Q" | "J") => false
    case (x, y)  => x.toInt > y.toInt
  }
}