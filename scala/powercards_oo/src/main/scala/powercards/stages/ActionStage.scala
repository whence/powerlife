package powercards.stages

import powercards.{ActionCard, Item, Game, Stage, choices }
import powercards.Functions.divide

object ActionStage extends Stage {
  def play(game: Game): Stage = {
    if (game.active.actions > 0) {
      game.active.chooseOptionalOne(message = "Select an action card to play",
        items = game.active.hand.map(Item.fromCard(_.isInstanceOf[ActionCard]))) match {
        case choices.optional_one.NonSelectable =>
          game.active.io.output("No action card to play. Skip to treasure stage")
          skip(game)
        case choices.optional_one.Skip =>
          game.active.io.output("Skip to treasure stage")
          skip(game)
        case choices.optional_one.One(index) =>
          game.active.actions -= 1
          val (card, hand) = divide(game.active.hand, index)
          game.active.hand = hand
          game.active.played :+= card
          game.active.io.output(s"Playing $card")
          card.asInstanceOf[ActionCard].play(game)
          this
      }
    } else {
      game.active.io.output("No action point. Skip to treasure stage")
      skip(game)
    }
  }

  private def skip(game: Game): Stage = {
    game.active.actions = 0
    TreasureStage
  }
}
