package sbt_demo_repositories

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class HelloRepositorySpec extends FlatSpec with Matchers with MockFactory {

  "hello" should "equals" in {
    HelloRepository.hello shouldBe "hello from HelloRepository with hello from Utils"
  }
}
