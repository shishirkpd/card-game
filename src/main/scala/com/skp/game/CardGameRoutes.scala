package com.skp.game

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, Route, RouteConcatenation}
import akka.util.Timeout
import com.skp.game.actors._
import com.skp.game.model.{ActionPerformed, GameType, User, UserResponse}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

import javax.ws.rs.core.MediaType
import spray.json.RootJsonFormat

import javax.ws.rs.{Consumes, GET, POST, Path, Produces}
import scala.concurrent.Future

case class CardGameRoutes(gameActor: ActorRef[Command])(implicit val system: ActorSystem[_]) extends RouteConcatenation{

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.skp.game.utils.JsonFormats._

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def createUser(user: User) : Future[ActionPerformed] = {
    gameActor.ask(CreateUser(user, _))
  }

  def getUser(name: String): Future[UserResponse] = {
    gameActor.ask(GetUser(name, _))
  }

  def play(name: String, gameType: GameType.Value): Future[ActionPerformed]  = gameActor.ask(Play(name, _, gameType))

  def fold(name: String, gameType: GameType.Value): Future[ActionPerformed]  = gameActor.ask(FoldGame(name, _, gameType))

  def show(name: String, gameType: GameType.Value): Future[ActionPerformed]  = gameActor.ask(Show(name, _, gameType))

  val routes : Route = pathPrefix("card-game")(gameDetails
    ~ createUsers
    ~ getUsersDetails
    ~ startGameType
    ~ playerFold
    ~ playerShow
  )


  @Path("/card-game")
  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Operation(summary = "GET Details about the game", description = "Details of the card game",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Details of the card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[ApiResponse])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def gameDetails: Route = pathEnd {
    get {
      complete((StatusCodes.OK, "Choose the game you want to play 1 card or 2 card."))
    }
  }

  @Path("/card-game/player")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Create players for the game", description = "Create players for card game",
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Create players for card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[ApiResponse])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def createUsers: Route = path("player") {
    post {
      entity(as[String]) { name =>
        val user = User(name)
        onSuccess(createUser(user)) { performed =>
          complete((StatusCodes.Created, performed))
        }
      }
    }
  }


  @Path("/card-game/player/{name}")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "GET players details", description = "Return the players details",
    parameters =  Array(
      new Parameter(name = "name", in = ParameterIn.PATH, required = true, description = "name of the players needs to be fetched",
        content = Array(new Content(schema = new Schema(implementation = classOf[String]))))
    ),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Details of the players for card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[ActionPerformed])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def getUsersDetails: Route = path("player" / Segment) { name =>
    get {
      rejectEmptyResponse {
        onSuccess(getUser(name)) { response =>
          complete(response.user)
        }
      }
    }
  }

  @Path("/card-game/{game-type}/player/{name}")
  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "POST players details for starting the game", description = "Return game status for the players",
    parameters =  Array(
      new Parameter(name = "game-type", in = ParameterIn.PATH, required = true,
        description = "game type player want to play 1 for one card game 2 for two card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
      new Parameter(name = "name", in = ParameterIn.PATH, required = true,
        description = "name of the players who want to play game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
    ),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Game details of the players for card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[ActionPerformed])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def startGameType: Route =
    path(Segment / "player" / Segment) { (gameType, name) =>
      post {
        rejectEmptyResponse {
          onSuccess(play(name, gameType.toInt match {
            case 1 => GameType.OneCard
            case 2 => GameType.TwoCard
          })) { response =>
            complete(response)
          }
        }
      }
    }

  @Path("/card-game/{game-type}/player/{name}/fold")
  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "POST players details for folding the game", description = "Players choose the option to fold the game",
    parameters =  Array(
      new Parameter(name = "game-type", in = ParameterIn.PATH, required = true,
        description = "game type player want to play 1 for one card game 2 for two card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
      new Parameter(name = "name", in = ParameterIn.PATH, required = true,
        description = "name of the players who want to play game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
    ),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Game Details of the players for card game on fold",
        content = Array(new Content(schema = new Schema(implementation = classOf[ActionPerformed])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def playerFold: Route =
    path(Segment / "player" / Segment / "fold") { (gameType, name) =>
      post {
        rejectEmptyResponse {
          onSuccess(fold(name, gameType.toInt match {
            case 1 => GameType.OneCard
            case 2 => GameType.TwoCard
          })) { response =>
            complete(response)
          }
        }
      }
    }

  @Path("/card-game/{game-type}/player/{name}/show")
  @POST
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "POST players details for show finish the game", description = "Players choose the option to show in the game",
    parameters =  Array(
      new Parameter(name = "game-type", in = ParameterIn.PATH, required = true,
        description = "game type player want to play 1 for one card game 2 for two card game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
      new Parameter(name = "name", in = ParameterIn.PATH, required = true,
        description = "name of the players who want to play game",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
    ),
    responses = Array(
      new ApiResponse(responseCode = "201", description = "Game Details of the players when he performed show ",
        content = Array(new Content(schema = new Schema(implementation = classOf[ActionPerformed])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def playerShow: Route =
    path(Segment / "player" / Segment / "show") { (gameType, name) =>
      post {
        rejectEmptyResponse {
          onSuccess(show(name, gameType.toInt match {
            case 1 => GameType.OneCard
            case 2 => GameType.TwoCard
          })) { response =>
            complete(response)
          }
        }
      }
    }

}
