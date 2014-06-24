package powercards

object Core {
  type Game = Map[Int, Box]

  trait Box
  case class CardBox(cards: Vector[Card]) extends Box
  case class IntBox(n: Int) extends Box
  case class StageBox(stage: Stage) extends Box
  case class StringBox(str: String) extends Box

  trait Stage
  case object ActionStage extends Stage
  case object TreasureStage extends Stage
  case object BuyStage extends Stage
  case object CleanupStage extends Stage
  
  trait Card { 
    def name: String
    def cost: Int
    override def toString(): String = name
  }
  
  trait Actionable {
    def play(game: Game): Game
  }
  
  trait Treasurable {
    def coins: Int
  }
  
  trait Victoriable {
    def vps: Int
  }
}