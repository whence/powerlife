package powercards

import util.Random.nextInt

class Game(val players: Vector[Player]) {
  private var activePlayerIndex = nextInt(players.length)
  def active = players(activePlayerIndex)
}
