import core._

object Program {
  def main(args: Array[String]) {
    val game = new Game
    val players = Vector("wes", "bec").map(Player(_, game))
    val channel = new ConsoleChannel()
    val context = new DispatchContext(channel)
    context.send(game, StartGame(players))
    print("press any key to start the game")
    channel.readInput()
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
