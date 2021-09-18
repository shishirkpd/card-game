package com.skp.game.model

case class User(name: String, tokens: Int = 1000, status: StatusEnum = LOBBY) {
  override def toString: String = {
    s"$name with token $tokens user is in $status"
  }
}

final case class ActionPerformed(description: String)

final case class UserResponse(user: Option[User])