package powercards

import org.scalatest._
import powercards.cards.{Estate, Copper}

class PlayerSpec extends FlatSpec with Matchers {
  "Player" should "initialise cards" in {
    val player = new Player("P1")
    player.played shouldBe empty
    player.discard shouldBe empty
    val fullDeck = player.deck ++ player.hand
    fullDeck.count {
      case x: Copper => true
      case _ => false
    } shouldBe 7

    fullDeck.count {
      case x: Estate => true
      case _ => false
    } shouldBe 3
  }
}
