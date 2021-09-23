package com.skp.game

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.skp.game.actors.GameActor
import com.skp.game.service.UserServiceImpl
import com.skp.game.swagger.{Site, SwaggerDocService}
import com.softwaremill.macwire.wire

import scala.util.{Failure, Success}

object CardGameApp extends RouteConcatenation with Site{

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/swagger", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val userService = wire[UserServiceImpl]
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val cardGameActor = context.spawn(GameActor(userService), "GameActor")
      context.watch(cardGameActor)

      val routes = (
        new CardGameRoutes(cardGameActor)(context.system).routes ~
        SwaggerDocService.routes ~
        site)
      startHttpServer(routes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "CardGameAkkaHttpServer")
  }
}
