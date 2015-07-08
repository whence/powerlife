package powercards

trait Card {
  def name: String = this.getClass.getSimpleName
  def cost: Int
  def calculateCost(game: Game): Int = cost
}

trait ActionCard extends Card {
  def play(game: Game): Unit
}

trait TreasureCard extends Card {
  def coins: Int
}

trait VictoryCard extends Card {
  def vps: Int
}

