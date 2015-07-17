package powercards

import org.scalatest._
import powercards.cards.{Estate, Copper}

import scala.collection.mutable

class PlayerSpec extends FlatSpec with Matchers {
  "Player" should "initialise cards" in {
    val player = new Player with ConsoleInteractive with ReplayableInteractive {
      val name = "P1"
      val inputQueue = mutable.Queue.empty[String]
      val throwOnEmptyQueue = true
    }
    player.played shouldBe empty
    player.discard shouldBe empty
    val fullDeck = player.deck ++ player.hand
    fullDeck.count(_.isInstanceOf[Copper]) shouldBe 7
    fullDeck.count(_.isInstanceOf[Estate]) shouldBe 3
  }
}
