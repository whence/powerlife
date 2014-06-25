package core

object Program {

  def main(args: Array[String]): Unit = {
    import Powercards._
    import collection.immutable.Queue
    
    @annotation.tailrec
    def inputloop(game: Game, userInputs: Queue[Vector[Int]]) {
      game.progress(userInputs) match {
        case (dialog, context) =>
          println(dialog.player.name + ": " + dialog.message)
          println(dialog.choices.mkString(", "))
          println(game.activePlayer)
          println("trash %s".format(game.trash))
          val userInput = readInput()
          inputloop(context.checkpoint, context.processedUserInputs.enqueue(userInput))
      }
    }
    
    def readInput(): Vector[Int] = {
      toVector(readLine().split(',').map(x => parseInt(x.trim)).collect({ case Some(index) => index }).toList)
    }
    
    def toVector[A](xs: List[A]): Vector[A] = {
      import collection.breakOut
      xs.map(identity)(breakOut)
    }
    
    def parseInt(s: String) = try { Some(s.toInt) } catch { case _ => None }
    
    inputloop({
      val game = Game.create(Vector("wes", "bec"))
      game.activePlayer.hand ++= Vector(new Remodel, new ThroneRoom, new ThroneRoom)
      game
    }, Queue.empty)
  }
}