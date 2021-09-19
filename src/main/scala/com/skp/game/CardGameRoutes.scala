package com.skp.game

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.skp.game.actors.{Command, CreateUser, FoldGame, GetUser, Play, Show}
import com.skp.game.model.{ActionPerformed, User, UserResponse}

import scala.concurrent.Future

case class CardGameRoutes(gameActor: ActorRef[Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.skp.game.utils.JsonFormats._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def createUser(user: User) : Future[ActionPerformed] = {
    gameActor.ask(CreateUser(user, _))
  }

  def getUser(name: String): Future[UserResponse] = {
    gameActor.ask(GetUser(name, _))
  }

  def play(name: String): Future[ActionPerformed]  = gameActor.ask(Play(name, _))

  def fold(name: String): Future[ActionPerformed]  = gameActor.ask(FoldGame(name, _))

  def show(name: String): Future[ActionPerformed]  = gameActor.ask(Show(name, _))

  val appRoutes: Route = pathPrefix("card-game") {
    concat(
      pathEnd {
        concat(
          get {
            complete((StatusCodes.OK, "Choose the game you want to play 1 card or 2 card."))
          },
          post {
            entity(as[String]) { name =>
              val user = User(name)
              onSuccess(createUser(user)) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }
          }
        )
      },
      pathPrefix("play") {
        concat(
          path(Segment) { name =>
            get {
              rejectEmptyResponse {
                onSuccess(play(name)) { response =>
                  complete(response)
                }
              }
            }
          },
          pathPrefix("fold") {
            concat(
              post {
                entity(as[String]) { name =>
                  onSuccess(fold(name)) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          pathPrefix("show") {
            concat(
              post {
                entity(as[String]) { name =>
                  onSuccess(show(name)) { response =>
                    complete(response)
                  }
                }
              }
            )
          }
        )
      },
      path(Segment) { name =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(getUser(name)) { response =>
                complete(response.user)
              }
            }
          })
      }
    )
  }

}
