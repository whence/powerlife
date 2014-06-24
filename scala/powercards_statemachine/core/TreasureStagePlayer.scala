package core

class TreasureStagePlayer(player: Player) extends SubReceiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Selector(player, "Select treasure cards to play",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Treasurable])),
        requirement = None))
      context.send(player, Perform)
    case NothingToSelect =>
      context.log("Nothing to select. Skip to next stage")
      player.popSub()
      context.send(player, NextStage)
    case SelectedNothing =>
      context.log("You have skipped to next stage")
      player.popSub()
      context.send(player, NextStage)
    case SelectedOne(index) =>
      player.hand.transferToEnd(index, player.played) match {
        case treasureCard: Treasurable =>
          context.send(player.game, StatChange(TurnStat(coins = treasureCard.coin), player))
      }
    case SelectedMulti(indexes) =>
      val treasureCards = player.hand.transferMultiToEnd(indexes, player.played) collect {
        case treasureCard: Treasurable => treasureCard
      }
      context.send(player.game, StatChange(TurnStat(coins = treasureCards.map(_.coin).sum), player))
    case Ack(StatChange(_, _)) =>
      player.popSub()
      context.send(player, Progress)
  }}
}
