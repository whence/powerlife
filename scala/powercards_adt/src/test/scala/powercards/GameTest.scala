package powercards

import org.scalatest.{Matchers, FlatSpec}

class GameTest extends FlatSpec with Matchers {
  it should "create game" in {
    val game = new Game(Seq("wes", "bec"))
  }

  it should "match card type" in {
    Cards.remodel.feature.isAction should be (true)
  }

  it should "equals cards" in {
    val cards = Vector(Cards.estate, Cards.copper, Cards.remodel)
    val card = cards(2)
    cards(2) should be (Cards.remodel)
  }
}
