/*
package com.skp.game.swagger

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.webjars.WebJarAssetLocator
import scala.util.{Failure, Success, Try}

object SwaggerUIRouting {

  final val SWAGGER_UI_INDEX_HTML_FILE_NAME = "swagger/index.html"

}

final case class SwaggerUIRouting( webJarAssetLocator: WebJarAssetLocator,
                                   swaggerJsonName: String = "api-docs/swagger.json") {

  def completeRoute: Route =
    indexPageRoute ~ webJarsRoute

  def indexPageRoute: Route = getFromResourceDirectory("swagger")  ~ path(PathEnd) {
    get {
      redirect("index.html", StatusCodes.PermanentRedirect)
    }
  }

  def webJarsRoute: Route = get {
    extractUnmatchedPath { path =>
      Try(webJarAssetLocator.getFullPath(path.toString)) match {
        case Success(fullPath) =>
          getFromResource(fullPath)
        case Failure(_: IllegalArgumentException) =>
          reject
        case Failure(e) =>
          failWith(e)
      }
    }
  }
}*/
