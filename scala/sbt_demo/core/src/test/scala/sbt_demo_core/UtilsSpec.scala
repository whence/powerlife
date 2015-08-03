package sbt_demo_core

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class UtilsSpec extends FlatSpec with Matchers with MockFactory {

  "hello" should "equals" in {
    Utils.hello shouldBe "hello from Utils"
  }
}
