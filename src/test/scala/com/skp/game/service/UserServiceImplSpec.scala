package com.skp.game.service

import com.skp.game.model.{User, WAITING}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserServiceImplSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  var userService = new UserServiceImpl

  override def beforeEach() {
    userService = new UserServiceImpl
  }

  "updateStatus" should {
    "it should update the user details if user exists" in {
      val user = User("Player1")
      userService.create(user)
      val updateUserStatus = user.copy(status = WAITING)
      userService.updateStatus(updateUserStatus)
      userService.findBy(user.name) shouldBe Some(updateUserStatus)
    }
  }
    "findBy" should {
      "it should return the user if exists" in {
        val user = User("Player1")
        userService.create(user)
        userService.findBy(user.name) shouldBe Some(user)
      }

      "it should return the none if user dose not exists exists" in {
        val user = User("Player1")
        userService.findBy(user.name) shouldBe None
      }
    }

    "create" should  {
      "it should create the user" in {
        val user = User("Player1")
        userService.create(user)
        userService.findBy(user.name) shouldBe Some(user)
      }
    }

}
