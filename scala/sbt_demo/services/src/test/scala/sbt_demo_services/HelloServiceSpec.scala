package sbt_demo_services

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class HelloServiceSpec extends FlatSpec with Matchers with MockFactory {

  "hello" should "equals" in {
    HelloService.hello shouldBe "hello from HelloService with hello from HelloRepository with hello from Utils with hello from Utils"
  }
}
