package com.skp.game.service

import com.skp.game.model.User

trait UserService {
  def findBy(name: String): Option[User]
  def create(user: User): User
  def updateStatus(user: User)
}

class UserServiceImpl extends UserService {

  private var users = Map[String, User]()
  override def findBy(name: String): Option[User] = {
    users.get(name) match {
      case None => None
      case user: Option[User] => user
    }
  }

  override def create(user: User): User = {
    findBy(user.name) match {
      case Some(user) => user
      case None => users+= ((user.name, user))
      user
    }
  }

  override def updateStatus(user: User): Unit = {
    users+= ((user.name, user))
  }
}
