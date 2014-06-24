package powercards

object program {
  def main(args: Array[String]): Unit = {
    val cards = Vector(Smithy, Copper)
    val treasureCards = cards.collect { case c: Treasurable => c }
    println(treasureCards)
  }
}

trait Stage
case class ActionStage(actions: Int, buys: Int, coins: Int) extends Stage
case class TreasureStage(buys: Int, coins: Int) extends Stage
case class BuyStage(buys: Int, coins: Int) extends Stage

class Game(val stage: Stage)

trait Card

trait Actionable {
  def play(game: Game): Game
}

trait Treasurable {
  def play(game: Game): Game
}

trait Victoriable {
  def calculatePoints(game: Game): Int
}

trait ActionCard extends Card with Actionable

trait BasicTreasureCard extends Card with Treasurable {
  val coins: Int

  def play(game: Game): Game = {
    game.stage match {
      case TreasureStage(buys, currentCoins) => new Game(TreasureStage(buys, currentCoins + this.coins))
    }
  }
}

trait BasicVictoryCard extends Card with Victoriable {
  val vps: Int
  def calculatePoints(game: Game): Int = vps
}

object Smithy extends ActionCard {
  def play(game: Game): Game = {
    game
  }
}

object Copper extends BasicTreasureCard {
  val coins: Int = 1
}
