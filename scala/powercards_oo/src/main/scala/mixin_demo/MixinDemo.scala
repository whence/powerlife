package mixin_demo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Card

trait Connection {
  def send(content: String)
  def receive(): String
}

class Player(val name: String) {
  def hand: Vector[Card] = Vector.empty
}

trait Interactive {
  def ask(question: String, items: Vector[String]): String
}

trait LocalInteractive extends Interactive {
  def ask(question: String, items: Vector[String]): String = {
    println(question)
    items.foreach(println)
    val result = io.StdIn.readLine()
    items.find(_ == result).get
  }
}

trait NetworkInteractive extends Interactive {
  def connection: Connection

  def ask(question: String, items: Vector[String]): String = {
    connection.send(s"$question\n${items.mkString("\n")}")
    val result = connection.receive()
    items.find(_ == result).get
  }
}

trait AIInteractive extends Interactive {
  def hand: Vector[Card]
  def allLogs: Seq[String]

  def ask(question: String, items: Vector[String]): String = {
    magic(allLogs, hand)
  }

  def magic(logs: Seq[String], hand: Vector[Card]): String
}

trait AuditingInteractive extends Interactive {
  def log(thing: String)

  abstract override def ask(question: String, items: Vector[String]): String = {
    val result = super.ask(question, items)
    log(result)
    result
  }
}

trait GlobalLogging {
  def logStore: mutable.Buffer[String]

  def log(thing: String) = logStore.append(thing)
  def allLogs: Seq[String] = logStore.toSeq
}

object SampleApp {
  val gameLog = new ArrayBuffer[String]
  val dummyConnection = new Connection {
    def send(content: String) = ???
    def receive(): String = ???
  }

  val localPlayer = new Player("local") with LocalInteractive with AuditingInteractive with GlobalLogging {
    val logStore = gameLog
  }

  val networkPlayer = new Player("network") with NetworkInteractive with AuditingInteractive with GlobalLogging {
    val connection = dummyConnection
    val logStore = gameLog
  }

  val aiPlayer = new Player("ai") with AIInteractive with AuditingInteractive with GlobalLogging {
    val logStore = gameLog
    def magic(logs: Seq[String], hand: Vector[Card]): String = ???
  }

  val players = Vector(localPlayer, networkPlayer, aiPlayer)
}
