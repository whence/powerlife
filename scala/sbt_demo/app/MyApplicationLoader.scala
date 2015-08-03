import play.api._
import play.api.ApplicationLoader.Context
import router.Routes

class MyApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new MyComponents(context).application
  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  lazy val router = new Routes(httpErrorHandler, applicationController)
  lazy val applicationController = new controllers.Application()
}
