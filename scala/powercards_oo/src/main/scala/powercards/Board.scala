package powercards

import scala.collection.mutable

class Board(numberOfPlayers: Int) {
  val trash = new mutable.Stack[Card]
}
