package powercards

object Cards {
  import Core._
  
  object Copper extends Card with Treasurable {
    val name = "Copper"
    val cost = 0
    val coins = 1
  }
  
  object Estate extends Card with Victoriable {
    val name = "Estate"
    val cost = 2
    val vps = 1
  }
  
  object Remodel extends Card with Actionable {
    val name = "Remodel"
    val cost = 4
    def play(game: Game): Game = game
  }
}