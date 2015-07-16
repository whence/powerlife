package powercards.cards

import powercards.{Game, BasicVictoryCard, ActionCard}

class Island extends ActionCard with BasicVictoryCard {
  val cost = 4
  val vps = 2

  def play(game: Game): Unit = ???
}
