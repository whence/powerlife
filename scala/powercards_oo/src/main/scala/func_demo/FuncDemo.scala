package func_demo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Card

trait Connection {
  def send(content: String) = ???
  def receive(): String = ???
}

object PlayerModule {
  case class Player(name: String, hand: Vector[Card])

  object LocalInteractive {
    def choose(player: Player, message: String, items: Vector[String]): String = {
      println(message)
      items.foreach(println)
      val result = io.StdIn.readLine()
      items.find(_ == result).get
    }
  }

  object NetworkInteractive {
    def choose(connection: Connection)
              (player: Player, message: String, items: Vector[String]): String = {
      connection.send(s"$message\n${items.mkString("\n")}")
      val result = connection.receive()
      items.find(_ == result).get
    }
  }

  object AIInteractive {
    def choose(allLogs: => Seq[String])
              (player: Player, message: String, items: Vector[String]): String = {
      magic(allLogs, player.hand)
      def magic(logs: Seq[String], hand: Vector[Card]): String = ???
    }
  }

  object AuditingInteractive {
    def choose(log: String => Unit)
              (choose: (Player, String, Vector[String]) => String)
              (player: Player, message: String, items: Vector[String]): String = {
      val result = choose(player, message, items)
      log(result)
      result
    }
  }

  object GlobalLogging {
    def log(logStore: mutable.Buffer[String])
           (thing: String) = {
      logStore.append(thing)
    }

    def allLogs(logStore: mutable.Buffer[String]): Seq[String] = {
      logStore.toSeq
    }
  }
}

object SampleApp {
  import PlayerModule._

  val gameLog = new ArrayBuffer[String]

  type Choose = (Player, String, Vector[String]) => String

  val localChoose: Choose = {
    val choose: Choose = LocalInteractive.choose
    val log = GlobalLogging.log(gameLog)
    AuditingInteractive.choose(log)(choose)
  }

  val networkChoose: Choose = {
    val choose: Choose = NetworkInteractive.choose(new Connection {})
    val log = GlobalLogging.log(gameLog)
    AuditingInteractive.choose(log)(choose)
  }

  val aiChoose: Choose = {
    def allLogs: Seq[String] = GlobalLogging.allLogs(gameLog)
    val choose: Choose = AIInteractive.choose(allLogs)
    val log = GlobalLogging.log(gameLog)
    AuditingInteractive.choose(log)(choose)
  }

  val chooses = Vector(localChoose, networkChoose, aiChoose)
}
