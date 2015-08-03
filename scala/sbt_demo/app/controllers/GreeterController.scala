package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import sbt_demo_core.Utils
import sbt_demo_services.HelloService

object GreeterController extends Controller {
  def greet = Action {
    Ok(Json.toJson(s"hello world and ${Utils.hello} and ${HelloService.hello}"))
  }
}
