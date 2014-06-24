package core

class Trasher(player: Player) extends SubReceiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Selector(player, "Select a card to trash",
        player.hand.toVector.map(card => new Selection(card.name, true)),
        requirement = Some(Set(1))))
      context.send(player, Perform)
    case NothingToSelect =>
      context.log("You have no card to trash")
      player.popSub()
      context.send(player, TrashedNothing)
    case SelectedOne(index) =>
      val card = player.hand.remove(index)
      context.log("Trashed " + card.name)
      context.send(player.game, PostCards(Vector(card), CardZoneName.Trash, player))
    case Ack(PostCards(cards, _, _)) =>
      player.popSub()
      context.send(player, TrashedOne(cards.head))
  } }
}

class Gainer(player: Player) extends SubReceiver {
  val fakeSupply: Vector[Card] = Vector(Copper, Estate, Remodel, ThroneRoom)

  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Selector(player, "Select a card to gain",
        fakeSupply.map(card => new Selection(card.name, true)),
        requirement = Some(Set(1))))
      context.send(player, Perform)
    case NothingToSelect =>
      context.log("You have no card to gain")
      player.popSub()
      context.send(player, GainedNothing)
    case SelectedOne(index) =>
      val card = fakeSupply(index)
      player.discard.append(card)
      context.log("Gained " + card.name)
      player.popSub()
      context.send(player, GainedOne(card))
  } }
}

class RemodelPlayer(player: Player, card: Card with Actionable)
  extends SubReceiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Trasher(player))
      context.send(player, Perform)
    case TrashedNothing =>
      player.popSub()
      context.send(player, ActionCardPlayed(card))
    case TrashedOne(trashedCard) =>
      player.pushSub(new Gainer(player))
      context.send(player, Perform)
    case GainedNothing | GainedOne(_) =>
      player.popSub()
      context.send(player, ActionCardPlayed(card))
  } }
}

class ThroneRoomPlayer(player: Player, card: Card with Actionable)
  extends SubReceiver {
  var playedCount = 0

  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      player.pushSub(new Selector(player, "Select an action card to play twice",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Actionable])),
        requirement = Some(Set(1))))
      context.send(player, Perform)
    case NothingToSelect =>
      context.log("You have no action card to play twice")
      player.popSub()
      context.send(player, ActionCardPlayed(card))
    case SelectedOne(index) =>
      player.hand.transferToEnd(index, player.played) match {
        case actionCard: Actionable =>
          context.log("playing " + actionCard.name + " first time")
          player.pushSub(actionCard.createSubReceiver(player, actionCard))
          context.send(player, Perform)
      }
    case ActionCardPlayed(actionCard) if playedCount == 0 =>
      playedCount += 1
      context.log("playing " + actionCard.name + " second time")
      player.pushSub(actionCard.createSubReceiver(player, actionCard))
      context.send(player, Perform)
    case ActionCardPlayed(actionCard) if playedCount == 1 =>
      playedCount += 1
      player.popSub()
      context.send(player, ActionCardPlayed(actionCard))
  } }
}