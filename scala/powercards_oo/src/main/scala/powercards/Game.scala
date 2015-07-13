package powercards

import powercards.stages.ActionStage

import util.Random.nextInt

class Game(val players: Vector[Player]) {
  private var activePlayerIndex = nextInt(players.length)
  def active = players(activePlayerIndex)
  private var stage: Stage = ActionStage

  def playOne(): Unit = {
    stage = stage.play(this)
  }
}
