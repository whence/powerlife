package powercards

trait Card {
  val name: String = this.getClass.getSimpleName
  def cost: Int
  def calculateCost(game: Game): Int = cost
}

trait ActionCard extends Card {
  def play(game: Game): Unit
}

trait TreasureCard extends Card {
  def play(game: Game): Unit
}

trait VictoryCard extends Card {
  def calculateVps(allCards: Vector[Card]): Int
}

trait Bulkable

trait BasicTreasureCard extends TreasureCard with Bulkable {
  def coins: Int
  def play(game: Game): Unit = {
    game.active.coins += coins
  }
}

trait BasicVictoryCard extends VictoryCard {
  def vps: Int
  def calculateVps(allCards: Vector[Card]): Int = vps
}

