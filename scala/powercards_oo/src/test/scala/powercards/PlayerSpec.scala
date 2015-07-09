package powercards

import org.scalatest._
import powercards.cards.{Estate, Copper}

class PlayerSpec extends FlatSpec with Matchers {
  "Player" should "initialise cards" in {
    val player = new Player("P1", new RecordedIO(Seq.empty))
    player.played shouldBe empty
    player.discard shouldBe empty
    val fullDeck = player.deck ++ player.hand
    fullDeck.count(_.isInstanceOf[Copper]) shouldBe 7
    fullDeck.count(_.isInstanceOf[Estate]) shouldBe 3
  }
}
