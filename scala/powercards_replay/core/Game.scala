package core

object Game {
  def apply(playerCount: Int): Game = {
    val initDeck = Seq.fill(3)(Estate) ++ Seq.fill(7)(Copper)

    val game = (0 until playerCount).foldLeft(new Game(
      Map.empty[Int, Vector[Card]],
      Map.empty[Int, Int])) { (game, playerIndex) =>
      val deck = util.Random.shuffle(initDeck)
      val zones = game.zones +
        ((Keys.deck + playerIndex) -> deck.drop(5).toVector) +
        ((Keys.hand + playerIndex) -> deck.take(5).toVector) +
        ((Keys.played + playerIndex) -> Vector.empty[Card]) +
        ((Keys.discard + playerIndex) -> Vector.empty[Card])

      val stats = game.stats +
        ((Keys.actions + playerIndex) -> 0) +
        ((Keys.buys + playerIndex) -> 0) +
        ((Keys.coins + playerIndex) -> 0)

      new Game(zones, stats)
    }

    val zones = game.zones + (Keys.trash -> Vector.empty[Card])
    val stats = game.stats +
      (Keys.playerCount -> playerCount) +
      (Keys.activePlayerIndex -> util.Random.nextInt(playerCount)) +
      (Keys.stage -> Keys.action)

    activatePlayer(new Game(zones, stats))
  }

  def activatePlayer(game: Game): Game = {
    val playerIndex = game.stats(Keys.activePlayerIndex)
    val stats = game.stats +
      ((Keys.actions + playerIndex) -> 1) +
      ((Keys.buys + playerIndex) -> 1) +
      ((Keys.coins + playerIndex) -> 0)
    new Game(game.zones, stats)
  }

  def deactivatePlayer(game: Game): Game = {
    val playerIndex = game.stats(Keys.activePlayerIndex)
    val stats = game.stats +
      ((Keys.actions + playerIndex) -> 0) +
      ((Keys.buys + playerIndex) -> 0) +
      ((Keys.coins + playerIndex) -> 0)
    new Game(game.zones, stats)
  }

  @annotation.tailrec
  final def drawCards(zones: Map[Int, Vector[Card]], playerIndex: Int, count: Int)
  : (Map[Int, Vector[Card]], Int) = {
    if (count > 0) {
      if (zones(Keys.deck + playerIndex).isEmpty) {
        if (zones(Keys.discard + playerIndex).isEmpty)
          (zones, count)
        else {
          drawCards(zones +
              ((Keys.discard + playerIndex) -> Vector.empty[Card]) +
              ((Keys.deck + playerIndex) -> util.Random.shuffle(zones(Keys.discard + playerIndex))),
            playerIndex, count)
        }
      } else {
        drawCards(zones +
          ((Keys.deck + playerIndex) -> zones(Keys.deck + playerIndex).init) +
          ((Keys.hand + playerIndex) -> (zones(Keys.hand + playerIndex) :+ zones(Keys.deck + playerIndex).last)),
          playerIndex, count - 1)
      }
    } else (zones, count)
  }

  @annotation.tailrec
  final def play(game: Game): Game = {
    game.stats(Keys.stage) match {
      case Keys.action =>
    }
  }
}

class Game(val zones: Map[Int, Vector[Card]], val stats: Map[Int, Int])