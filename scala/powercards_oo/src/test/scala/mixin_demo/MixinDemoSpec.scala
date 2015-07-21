package mixin_demo

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

import scala.collection.mutable.ArrayBuffer

class MixinDemoSpec extends FlatSpec with Matchers with MockFactory {

  "NetworkInteractive" should "choose" in {
    val interactive = new NetworkInteractive {
      val connection = mock[Connection]
    }

    (interactive.connection.send _).expects("question\nItem1\nItem2")
    (interactive.connection.receive _).expects().returning("Item2")

    interactive.ask("question", Vector("Item1", "Item2")) shouldBe "Item2"
  }

  "AIPlayer" should "have access to all logs" in {
    val gameLog = new ArrayBuffer[String]

    val player1 = new Player("player1") with NetworkInteractive with AuditingInteractive with GlobalLogging {
      val connection = mock[Connection]
      val logStore = gameLog
    }

    val player2 = new Player("player2") with NetworkInteractive with AuditingInteractive with GlobalLogging {
      val connection = mock[Connection]
      val logStore = gameLog
    }

    val mockedMagic = mockFunction[Seq[String], Vector[Card], String]
    val aiPlayer = new Player("ai") with AIInteractive with AuditingInteractive with GlobalLogging {
      val logStore = gameLog
      def magic(logs: Seq[String], hand: Vector[Card]): String = mockedMagic(logs, hand)
    }

    (player1.connection.send _).expects(*)
    (player2.connection.send _).expects(*)

    inSequence {
      (player1.connection.receive _).expects().returning("Item2")
      (player2.connection.receive _).expects().returning("Item3")
      mockedMagic.expects(Seq("Item2", "Item3"), *).returning("Item1").once()
      mockedMagic.expects(Seq("Item2", "Item3", "Item1"), *).returning("Item1").once()
    }

    player1.ask("question", Vector("Item1", "Item2", "Item3")) shouldBe "Item2"
    player2.ask("question", Vector("Item1", "Item2", "Item3")) shouldBe "Item3"
    aiPlayer.ask("question", Vector("Item1", "Item2", "Item3")) shouldBe "Item1"
    aiPlayer.ask("question", Vector("Item1", "Item2", "Item3")) shouldBe "Item1"
  }
}
