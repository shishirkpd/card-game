package com.skp.game.swagger

import akka.http.scaladsl.server.Directives

trait Site extends Directives {
  val site =
    path("swagger") { getFromResource("swagger/index.html") } ~
      getFromResourceDirectory("swagger")
}