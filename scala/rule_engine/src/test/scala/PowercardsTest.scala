import org.scalatest.{Matchers, FlatSpec}
import powercards._

class PowercardsTest extends FlatSpec with Matchers {
  it should "create game" in {
    val game = new Game(Seq("wes", "bec"))
  }

  it should "match card type" in {
    Cards.isAction(Cards.remodel) should be (true)
  }

  it should "equals cards" in {
    val cards = Vector(Cards.estate, Cards.copper, Cards.remodel)
    val card = cards(2)
    cards(2) should be (Cards.remodel)
  }
}
