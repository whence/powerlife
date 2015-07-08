package powercards

import org.scalatest._
import powercards.cards.{Silver, Copper}

class PileSpec extends FlatSpec with Matchers {
  "A pile" should "accept card of the same type" in {
    val pile = new Pile(() => new Copper, 10)
    pile.push(new Copper)
    pile.size should be (11)
  }

  "A pile" should "not accept card of the other type" in {
    val pile = new Pile(() => new Copper, 10)
    a [IllegalArgumentException] should be thrownBy {
      pile.push(new Silver)
    }
  }
}
