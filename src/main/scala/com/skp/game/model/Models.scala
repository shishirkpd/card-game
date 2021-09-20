package com.skp.game.model

import com.skp.game.model.PlayingCard.NumberCard

case class User(name: String, tokens: Int = 1000, status: UserStatus = LOBBY) {
  override def toString: String = {
    s"$name with token $tokens user is in $status"
  }
}

case class Player(user: User, card: List[NumberCard])

final case class ActionPerformed(description: String)

final case class UserResponse(user: Option[User])