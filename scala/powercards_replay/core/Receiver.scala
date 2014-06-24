package core

class ActionCardPlayer(private[this] val player: Player) extends Receiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Selector(player.name, "Select an action card to play",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Actionable])),
        requirement = Some(Set(0, 1)), parent = this), Perform)
    case NothingToSelect =>
      context.log("No action card to play. Skip to next stage")
      context.send(player, NextStage)
    case SelectedNothing =>
      context.log("You have skipped to next stage")
      context.send(player, NextStage)
    case SelectedOne(index) =>
      player.hand.transferToEnd(index, player.played) match {
        case actionCard: Actionable =>
          context.log("playing " + actionCard.name)
          context.send(actionCard.createReceiver(player, parent = this), Perform)
      }
    case ActionCardPlayed =>
      player.stat.actions -= 1
      context.send(player, Progress)
  } }
}

class TreasureCardPlayer(private[this] val player: Player) extends Receiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Selector(player.name, "Select treasure cards to play",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Treasurable])),
        requirement = None, parent = this), Perform)
    case NothingToSelect =>
      context.log("No treasure card to play. Skip to next stage")
      context.send(player, NextStage)
    case SelectedNothing =>
      context.log("You have skipped to next stage")
      context.send(player, NextStage)
    case SelectedOne(index) =>
      player.hand.transferToEnd(index, player.played) match {
        case treasureCard: Treasurable =>
          player.stat.coins += treasureCard.coin
          context.send(player, Progress)
      }
    case SelectedMulti(indexes) =>
      player.hand.transferMultiToEnd(indexes, player.played) foreach {
        case treasureCard: Treasurable =>
          player.stat.coins += treasureCard.coin
      }
      context.send(player, Progress)
  } }
}

class Selector(
  val playerName: String,
  val dialogTitle: String,
  val selections: Vector[Selection],
  val requirement: Option[Set[Int]],
  private[this] val parent: Receiver)
  extends Receiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      (requirement, selections.count(x => x.selectable)) match {
        case (_, 0) =>
          context.send(parent, NothingToSelect)
        case (Some(requireCounts), maxCount) if maxCount < requireCounts.min =>
          context.send(parent, NothingToSelect)
        case _ =>
          val dialogMessage = new StringBuilder()
          appendSelectionDialog(dialogMessage)
          context.send(context, InputRequired(dialogMessage.toString(), this))
      }
    case InputArrived(input) =>
      val selectedIndexes = input.split(',').map(x => x.trim).filter(x => !x.isEmpty).map(x => x.toInt).distinct.intersect(
        selections.zipWithIndex.filter(x => x._1.selectable).map(x => x._2))

      def reply() {
        selectedIndexes.length match {
          case 0 => context.send(parent, SelectedNothing)
          case 1 => context.send(parent, SelectedOne(selectedIndexes.head))
          case _ => context.send(parent, SelectedMulti(selectedIndexes.toVector))
        }
      }

      (requirement, selectedIndexes.length) match {
        case (None, _) => reply()
        case (Some(requireCounts), actualCount) if requireCounts(actualCount) => reply()
        case (Some(requireCounts), actualCount) if !requireCounts(actualCount) =>
          val dialogMessage = new StringBuilder("you do not make the required number of selection" + Utils.newline)
          appendSelectionDialog(dialogMessage)
          context.send(context, InputRequired(dialogMessage.toString(), this))
      }
  } }

  private[this] def appendSelectionDialog(builder: StringBuilder) {
    builder.append(playerName + ": " + dialogTitle + Utils.newline)
    selections.zipWithIndex.filter(x => x._1.selectable).foreach { x =>
      builder.append(x._2)
      builder.append(": ")
      builder.append(x._1.title)
      builder.append(Utils.newline)
    }
  }
}

class Selection(val title: String, val selectable: Boolean)

class Trasher(private[this] val player: Player, val parent: Receiver) extends Receiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Selector(player.name, "Select a card to trash",
        player.hand.toVector.map(card => new Selection(card.name, true)),
        requirement = Some(Set(1)), parent = this), Perform)
    case NothingToSelect =>
      context.log("You have no card to trash")
      context.send(parent, TrashedNothing)
    case SelectedOne(index) =>
      //val card = player.hand.transferToEnd(index, context.game.trash)
      //context.log("Trashed " + card.name)
      //context.send(parent, TrashedOne(card))
  } }
}

class Gainer(private[this] val player: Player, val parent: Receiver) extends Receiver {
  val fakeSupply: Vector[Card] = Vector(Copper, Estate, Remodel, ThroneRoom)

  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Selector(player.name, "Select a card to gain",
        fakeSupply.map(card => new Selection(card.name, true)),
        requirement = Some(Set(1)), parent = this), Perform)
    case NothingToSelect =>
      context.log("You have no card to gain")
      context.send(parent, GainedNothing)
    case SelectedOne(index) =>
      val card = fakeSupply(index)
      player.discard.append(card)
      context.log("Gained " + card.name)
      context.send(parent, GainedOne(card))
  } }
}

class RemodelPlayer(private[this] val player: Player, val parent: Receiver) extends Receiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Trasher(player, this), Perform)
    case TrashedNothing =>
      context.send(parent, ActionCardPlayed)
    case TrashedOne(card) =>
      context.send(new Gainer(player, this), Perform)
    case GainedNothing | GainedOne(_) =>
      context.send(parent, ActionCardPlayed)
  } }
}

class ThroneRoomPlayer(private[this] val player: Player, val parent: Receiver) extends Receiver {
  private[this] var selectedActionCard: Option[Card with Actionable] = None

  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      context.send(new Selector(player.name, "Select an action card to play twice",
        player.hand.toVector.map(card => new Selection(card.name, card.isInstanceOf[Actionable])),
        requirement = Some(Set(1)), parent = this), Perform)
    case NothingToSelect =>
      context.log("You have no action card to play twice")
      context.send(parent, ActionCardPlayed)
    case SelectedOne(index) =>
      player.hand.transferToEnd(index, player.played) match {
        case actionCard: Actionable =>
          selectedActionCard = Some(actionCard)
          context.log("playing " + actionCard.name + " first time")
          context.send(actionCard.createReceiver(player, this), Perform)
      }
    case ActionCardPlayed =>
      selectedActionCard match {
        case Some(actionCard) =>
          selectedActionCard = None
          context.log("playing " + actionCard.name + " second time")
          context.send(actionCard.createReceiver(player, this), Perform)
        case None =>
          context.send(parent, ActionCardPlayed)
      }
  } }
}