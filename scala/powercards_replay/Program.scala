import core._

object Program {
  def main(args: Array[String]) {
    val playerNames = Vector("wes", "bec")
    val game = Game(playerNames.length)
  }
}

class ConsoleChannel extends Channel {
  var subscribers = Vector.empty[String => (String, Boolean)]

  def subscribeInput(subscriber: String => (String, Boolean)) {
    subscribers :+= subscriber
  }

  @annotation.tailrec
  final def readInput() {
    if (!subscribers.isEmpty) {
      readLine() match {
        case "exit" =>
          println("terminated as requested")
        case line =>
          subscribers = subscribers filter { subscriber =>
            val output = subscriber(line)
            print(output._1)
            output._2
          }
          readInput()
      }
    } else {
      println("terminated as all subscribers left")
    }
  }
}
