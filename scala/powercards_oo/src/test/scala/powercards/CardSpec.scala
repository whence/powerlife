package powercards

import org.scalatest._
import powercards.cards.{Island, Estate, Silver}

import scala.collection.mutable

class CardSpec extends FlatSpec with Matchers {
  val player1 = new Player with ConsoleInteractive with ReplayableInteractive {
    val name = "P1"
    val inputQueue = mutable.Queue.empty[String]
    val throwOnEmptyQueue = true
  }

  val player2 = new Player with ConsoleInteractive with ReplayableInteractive {
    val name = "P2"
    val inputQueue = mutable.Queue.empty[String]
    val throwOnEmptyQueue = true
  }

  val players = Vector(player1, player2)
  val game = new Game(players, new SinkLogger)

  "Silver" should "have a real cost of 3" in {
    new Silver().calculateCost(game) shouldBe 3
  }

  "Estate" should "have name of Estate" in {
    new Estate().name shouldBe "Estate"
  }

  "Mixin" should "work properly" in {
    val card = new Island
    card.name shouldBe "Island"
    card.cost shouldBe 4
    card.calculateCost(game) shouldBe 4
    card.vps shouldBe 2
    card.calculateVps(Vector.empty) shouldBe 2

    // cast
    val c1: Card = card
    val c2: ActionCard = card
    val c3: VictoryCard = card
    val c4: BasicVictoryCard = card
    val c5: Card with ActionCard = card
    val c6: Card with VictoryCard = card
    val c7: Card with BasicVictoryCard = card
    val c8: ActionCard with VictoryCard = card
    val c9: VictoryCard with Card = card
    val cards = Vector(c1, c2, c3, c4, c5, c6, c7, c8, c9)

    // isInstanceOf
    card.isInstanceOf[Card] shouldBe true
    card.isInstanceOf[ActionCard] shouldBe true
    card.isInstanceOf[VictoryCard] shouldBe true
    card.isInstanceOf[BasicVictoryCard] shouldBe true
    card.isInstanceOf[Card with ActionCard] shouldBe true
    card.isInstanceOf[Card with VictoryCard] shouldBe true
    card.isInstanceOf[Card with BasicVictoryCard] shouldBe true
    card.isInstanceOf[ActionCard with VictoryCard] shouldBe true
    card.isInstanceOf[VictoryCard with Card] shouldBe true
    card.isInstanceOf[TreasureCard] shouldBe false
    card.isInstanceOf[BasicTreasureCard] shouldBe false
    card.isInstanceOf[Card with TreasureCard] shouldBe false

    c1.isInstanceOf[Card] shouldBe true
    c1.isInstanceOf[ActionCard] shouldBe true
    c1.isInstanceOf[VictoryCard] shouldBe true
    c1.isInstanceOf[BasicVictoryCard] shouldBe true
    c1.isInstanceOf[Card with ActionCard] shouldBe true
    c1.isInstanceOf[Card with VictoryCard] shouldBe true
    c1.isInstanceOf[Card with BasicVictoryCard] shouldBe true
    c1.isInstanceOf[ActionCard with VictoryCard] shouldBe true
    c1.isInstanceOf[VictoryCard with Card] shouldBe true
    c1.isInstanceOf[TreasureCard] shouldBe false
    c1.isInstanceOf[BasicTreasureCard] shouldBe false
    c1.isInstanceOf[Card with TreasureCard] shouldBe false

    c6.isInstanceOf[Card] shouldBe true
    c6.isInstanceOf[ActionCard] shouldBe true
    c6.isInstanceOf[VictoryCard] shouldBe true
    c6.isInstanceOf[BasicVictoryCard] shouldBe true
    c6.isInstanceOf[Card with ActionCard] shouldBe true
    c6.isInstanceOf[Card with VictoryCard] shouldBe true
    c6.isInstanceOf[Card with BasicVictoryCard] shouldBe true
    c6.isInstanceOf[ActionCard with VictoryCard] shouldBe true
    c6.isInstanceOf[VictoryCard with Card] shouldBe true
    c6.isInstanceOf[TreasureCard] shouldBe false
    c6.isInstanceOf[BasicTreasureCard] shouldBe false
    c6.isInstanceOf[Card with TreasureCard] shouldBe false

    c9.isInstanceOf[Card] shouldBe true
    c9.isInstanceOf[ActionCard] shouldBe true
    c9.isInstanceOf[VictoryCard] shouldBe true
    c9.isInstanceOf[BasicVictoryCard] shouldBe true
    c9.isInstanceOf[Card with ActionCard] shouldBe true
    c9.isInstanceOf[Card with VictoryCard] shouldBe true
    c9.isInstanceOf[Card with BasicVictoryCard] shouldBe true
    c9.isInstanceOf[ActionCard with VictoryCard] shouldBe true
    c9.isInstanceOf[VictoryCard with Card] shouldBe true
    c9.isInstanceOf[TreasureCard] shouldBe false
    c9.isInstanceOf[BasicTreasureCard] shouldBe false
    c9.isInstanceOf[Card with TreasureCard] shouldBe false

    // collect
    cards.collect { case x: ActionCard => x } shouldBe cards
    cards.collect { case x: VictoryCard => x } shouldBe cards
    cards.collect { case x: ActionCard with VictoryCard => x } shouldBe cards
    cards.collect { case x: ActionCard with BasicVictoryCard => x } shouldBe cards
    cards.collect { case x: VictoryCard with ActionCard => x } shouldBe cards
    cards.collect { case x: BasicVictoryCard with ActionCard => x } shouldBe cards
    cards.collect { case x: TreasureCard => x } shouldBe empty
    cards.collect { case x: ActionCard with TreasureCard => x } shouldBe empty
    cards.collect { case x: TreasureCard with ActionCard => x } shouldBe empty
    cards.collect { case x: ActionCard with BasicTreasureCard => x } shouldBe empty
    cards.collect { case x: BasicTreasureCard with ActionCard => x } shouldBe empty

    Vector(c3).collect { case x: ActionCard => x } shouldBe Vector(c3)
    Vector(c3).collect { case x: ActionCard with BasicVictoryCard => x } shouldBe Vector(c3)
    Vector(c3).collect { case x: ActionCard with BasicTreasureCard => x } shouldBe empty
  }
}
