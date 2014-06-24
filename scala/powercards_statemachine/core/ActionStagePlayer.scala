package core

class ActionStagePlayer(player: Player) extends SubReceiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Selector(player, "Select an action card to play",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Actionable])),
        requirement = Some(Set(0, 1))))
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
        case actionCard: Actionable =>
          context.log("playing " + actionCard.name)
          player.pushSub(actionCard.createSubReceiver(
            player = player,
            card = actionCard))
          context.send(player, Perform)
      }
    case ActionCardPlayed(card) =>
      context.send(player.game, StatChange(TurnStat(actions =  -1), player))
    case Ack(StatChange(_, _)) =>
      player.popSub()
      context.send(player, Progress)
  }}
}
