package com.skp.game.service

import com.skp.game.actors.CardGameActor.getClass
import com.skp.game.model.User
import org.slf4j.LoggerFactory

trait UserService {
  def findBy(name: String): Option[User]
  def create(user: User): User
  def updateStatus(user: User)
}

class UserServiceImpl extends UserService {

  private var users = Map[String, User]()
  private val logger = LoggerFactory.getLogger(getClass)

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
    logger.info(s"Updating user: $user")
    users+= ((user.name, user))
  }
}
