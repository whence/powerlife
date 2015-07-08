package powercards

import org.scalatest._
import powercards.cards.{Estate, Silver}

class CardSpec extends FlatSpec with Matchers {
  val game = new Game(Seq("P1", "P2"))

  "Silver" should "have a real cost of 3" in {
    new Silver().calculateCost(game) should be (3)
  }

  "Estate" should "have name of Estate" in {
    new Estate().name should be ("Estate")
  }
}
