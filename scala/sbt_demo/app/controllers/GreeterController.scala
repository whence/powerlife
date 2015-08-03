package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object GreeterController extends Controller {
  def greet = Action {
    Ok(Json.toJson("hello world"))
  }
}
