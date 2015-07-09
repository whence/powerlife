package powercards

import util.Random.nextInt

class Game(playerNames: Seq[String]) {
  val players: Vector[Player] = playerNames.map(new Player(_)).toVector
  private var activePlayerIndex = nextInt(players.length)
  def active = players(activePlayerIndex)
}
