package java_demo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Card

trait Connection {
  def send(content: String) = ???
  def receive(): String = ???
}

class Player(val name: String, val interactive: Interactive) {
  def hand: Vector[Card] = Vector.empty
}

trait Interactive {
  def choose(title: String, items: Vector[String]): String
}

class LocalInteractive extends Interactive {
  def choose(message: String, items: Vector[String]): String = {
    println(message)
    items.foreach(println)
    val result = io.StdIn.readLine()
    items.find(_ == result).get
  }
}

class NetworkInteractive(connection: Connection) extends Interactive {
  def choose(message: String, items: Vector[String]): String = {
    connection.send(s"$message\n${items.mkString("\n")}")
    val result = connection.receive()
    items.find(_ == result).get
  }
}

class AIInteractive(logRetriever: LogRetriever) extends Interactive {
  val hand: Vector[Card] = ??? // How to get my cards???

  def choose(message: String, items: Vector[String]): String = {
    magic(logRetriever.allLogs, hand)

    def magic(logs: Seq[String], hand: Vector[Card]): String = ???
  }
}

class AuditingInteractive(logger: Logger, interactive: Interactive) extends Interactive {
  def choose(message: String, items: Vector[String]): String = {
    val result = interactive.choose(message, items)
    logger.log(result)
    result
  }
}

trait Logger {
  def log(thing: String)
}

trait LogRetriever {
  def allLogs: Seq[String]
}

class GlobalLoger(logStore: mutable.Buffer[String]) extends Logger with LogRetriever {
  def log(thing: String) = logStore.append(thing)
  def allLogs: Seq[String] = logStore.toSeq
}

object SampleApp {
  val gameLog = new ArrayBuffer[String]

  val localPlayer = new Player(
    name = "local",
    interactive = new AuditingInteractive(
      logger = new GlobalLoger(
        logStore = gameLog
      ),
      interactive = new LocalInteractive
    )
  )

  val networkPlayer = new Player(
    name = "network",
    interactive = new AuditingInteractive(
      logger = new GlobalLoger(
        logStore = gameLog
      ),
      interactive = new NetworkInteractive(
        connection = new Connection {}
      )
    )
  )

  val aiPlayer = new Player(
    name = "ai",
    interactive = new AuditingInteractive(
      logger = new GlobalLoger(
        logStore = gameLog
      ),
      interactive = new AIInteractive(
        logRetriever = new GlobalLoger(
          logStore = gameLog
        )
      )
    )
  )
}