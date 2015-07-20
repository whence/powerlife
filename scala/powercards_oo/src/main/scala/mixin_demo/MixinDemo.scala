package mixin_demo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Card

trait Connection {
  def send(content: String) = ???
  def receive(): String = ???
}

trait Player extends Interactive {
  def name: String
  def hand: Vector[Card] = Vector.empty
}

trait Interactive {
  def choose(title: String, items: Vector[String]): String
}

trait LocalInteractive extends Interactive {
  def choose(message: String, items: Vector[String]): String = {
    println(message)
    items.foreach(println)
    val result = io.StdIn.readLine()
    items.find(_ == result).get
  }
}

trait NetworkInteractive extends Interactive {
  def connection: Connection

  def choose(message: String, items: Vector[String]): String = {
    connection.send(s"$message\n${items.mkString("\n")}")
    val result = connection.receive()
    items.find(_ == result).get
  }
}

trait AIInteractive extends Interactive {
  def hand: Vector[Card]
  def allLogs: Seq[String]

  def choose(message: String, items: Vector[String]): String = {
    magic(allLogs, hand)

    def magic(logs: Seq[String], hand: Vector[Card]): String = ???
  }
}

trait AuditingInteractive extends Interactive {
  def log(thing: String)

  def choose(message: String, items: Vector[String]): String = {
    val result = super.choose(message, items)
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

  val localPlayer = new Player with LocalInteractive with AuditingInteractive with GlobalLogging {
    val name = "local"
    val logStore = gameLog
  }

  val networkPlayer = new Player with NetworkInteractive with AuditingInteractive with GlobalLogging {
    val name = "network"
    val connection = new Connection {}
    val logStore = gameLog
  }

  val aiPlayer = new Player with AIInteractive with AuditingInteractive with GlobalLogging {
    val name = "ai"
    val logStore = gameLog
  }

  val players = Vector(localPlayer, networkPlayer, aiPlayer)

}
