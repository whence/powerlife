package powercards.stages

import powercards.{ActionCard, Item, Game, Stage, choices }
import powercards.Functions.divide

object ActionStage extends Stage {
  def play(game: Game): Stage = {
    if (game.active.actions > 0) {
      game.active.chooseOptionalOne(message = "Select an action card to play",
        items = game.active.hand.map(c => new Item(c.name, c.isInstanceOf[ActionCard]))) match {
        case choices.optional_one.NonSelectable =>
          game.log("No action card to play. Skip to treasure stage")
          skip(game)
        case choices.optional_one.Skip =>
          game.log("Skip to treasure stage")
          skip(game)
        case choices.optional_one.One(index) =>
          game.active.actions -= 1
          val (card, hand) = divide(game.active.hand, index)
          game.active.hand = hand
          game.active.played :+= card
          game.log(s"Playing $card")
          card.asInstanceOf[ActionCard].play(game)
          this
      }
    } else {
      game.log("No action point. Skip to treasure stage")
      skip(game)
    }
  }

  private def skip(game: Game): Stage = {
    game.active.actions = 0
    TreasureStage
  }
}
