package powercards

class Game(playerNames: Seq[String]) {
  val players: Vector[Player] = playerNames.map(new Player(_)).toVector
}
