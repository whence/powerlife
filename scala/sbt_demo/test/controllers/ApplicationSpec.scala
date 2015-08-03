package controllers

import scala.concurrent.Future
import org.scalatest._
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends PlaySpec with Results {
  "Application#index" should {
    "should be valid" in {
      val controller = new Application()
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val jsonResult = contentAsJson(result)
      (jsonResult.as[String]) mustBe "hello world and hello from Utils and hello from HelloService with hello from HelloRepository with hello from Utils with hello from Utils"
    }
  }
}
