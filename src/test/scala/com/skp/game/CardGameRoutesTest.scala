package com.skp.game

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.skp.game.model.User
import com.skp.game.service.UserServiceImpl
import com.softwaremill.macwire.wire
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CardGameRoutesTest extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val userService: UserServiceImpl = wire[UserServiceImpl]
  val cardGame: ActorRef[CardGameActor.Command] = testKit.spawn(CardGameActor(userService))
  lazy val routes: Route = CardGameRoutes(cardGame).appRoutes

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

    "return the user details when added (POST /card-game)" in {
      val user = User("Player1")
      val userEntity = Marshal(user).to[MessageEntity].futureValue

      val request = Post("/card-game").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"User create with details: {\"name\":\"Player1\",\"status\":\"LOBBY\",\"tokens\":1000} with token 1000 user is in LOBBY"}""")
      }
    }
  }
}
