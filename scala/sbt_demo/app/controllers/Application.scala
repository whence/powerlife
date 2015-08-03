package controllers

import org.joda.time.LocalDateTime
import play.api._
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import play.api.libs._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("hello"))
  }
}
