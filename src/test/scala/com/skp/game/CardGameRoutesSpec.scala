package com.skp.game

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.skp.game.actors.{Command, GameActor}
import com.skp.game.model.{LOBBY, PLAYING, User, WAITING}
import com.skp.game.service.UserServiceImpl
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

class CardGameRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val userService: UserServiceImpl = mock[UserServiceImpl]
  val cardGame: ActorRef[Command] = testKit.spawn(actors.GameActor(userService))
  lazy val routes: Route = CardGameRoutes(cardGame).routes

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.skp.game.utils.JsonFormats._

  "CardGameRoutes" should {
    "return response for  (GET /card-game)" in {
      val request = HttpRequest(uri = "/card-game")

      request ~> routes ~> check {
        status should === (StatusCodes.OK)

        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        entityAs[String] should ===("""Choose the game you want to play 1 card or 2 card.""")
      }
    }

    "return the user details when added (POST /card-game/player)" in {
      val user = "Player1"
      val userEntity = Marshal(user).to[MessageEntity].futureValue

      val request = Post("/card-game/player").withEntity(userEntity)
      when(userService.create(any[User])).thenReturn(User(user))
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"User create with details: Player1 with token 1000 user is in LOBBY"}""")
      }
    }

    "return the response to user when user not exist (POST /card-game/player/Player1)" in {
      val request = HttpRequest(uri = "/card-game/player/Player1")

      when(userService.findBy(any[String])).thenReturn(None)
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Details for Player1 not found"}""")
      }
    }

    "return the response to user when user exist WAITING (POST /card-game/1/player/Player1)" in {
      val request = Post("/card-game/1/player/Player1")

      when(userService.findBy("Player1")).thenReturn(Option(User("Player1", status = WAITING)))
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Waiting for opponent to join"}""")
      }
    }

    "return the response to user when user exist and is in LOBBY (POST /card-game/1/player/Player1)" in {
      val request = Post("/card-game/1/player/Player1")

      when(userService.findBy("Player1")).thenReturn(Option(User("Player1", status = LOBBY)))
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Waiting for opponent to join"}""")
      }
    }

    "return the response to user when user exist and is in PLAYING (Post /card-game/1/player/Player1)" in {
      val request = Post("/card-game/1/player/Player1")

      when(userService.findBy("Player1")).thenReturn(Option(User("Player1", status = PLAYING)))
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Game in progress"}""")
      }
    }
  }
}
