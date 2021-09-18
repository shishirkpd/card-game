package com.skp.game

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.skp.game.service.UserServiceImpl
import com.softwaremill.macwire.wire

import scala.util.{Failure, Success}

object CardGameApp {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val userService = wire[UserServiceImpl]
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val cardGameActor = context.spawn(CardGameActor(userService), "CardGameActor")
      context.watch(cardGameActor)

      val routes = new CardGameRoutes(cardGameActor)(context.system)
      startHttpServer(routes.appRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "CardGameAkkaHttpServer")
    //#server-bootstrapping
  }
}
