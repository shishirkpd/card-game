package com.skp.game.utils

import com.skp.game.model._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

object JsonFormats {

  import DefaultJsonProtocol._

  implicit object StateJsonFormat extends RootJsonFormat[StatusEnum] {
    def write(s: StatusEnum) = s match {
      case LOBBY => JsString("LOBBY")
      case WAITING => JsString("WAITING")
      case PLAYING => JsString("PLAYING")
    }

    def read(value: JsValue) = value match {
      case JsString(x) if x == "LOBBY" => LOBBY
      case JsString(x) if x == "WAITING" => WAITING
      case JsString(x) if x == "PLAYING" => PLAYING
    }
  }

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
