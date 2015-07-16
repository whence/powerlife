package powercards

import org.scalatest._
import powercards.cards.{Estate, Silver, Copper}

class PileSpec extends FlatSpec with Matchers {
  "Pile" should "accept card of the same type" in {
    val pile = new Pile(() => new Copper, 10)
    pile.push(new Copper)
    pile.size shouldBe 11
  }

  "Pile" should "not accept card of the other type" in {
    val pile = new Pile(() => new Copper, 10)
    a [IllegalArgumentException] shouldBe thrownBy {
      pile.push(new Silver)
    }
  }

  "Pile" should "pop the same card that is just pushed and not sample" in {
    val pile = new Pile(() => new Estate, 10)
    val card = new Estate
    pile.push(card)
    val card2 = pile.pop()
    card2 shouldBe card
    card2 should not be pile.sample
  }

  "Pile" should "not be able to pop if empty" in {
    val pile = new Pile(() => new Estate, 0)
    pile shouldBe empty
    a [IllegalStateException] shouldBe thrownBy {
      pile.pop()
    }
  }
}
