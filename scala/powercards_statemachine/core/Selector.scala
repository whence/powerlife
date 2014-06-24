package core

class Selector(
    player: Player,
    dialogTitle: String,
    selections: Vector[Selection],
    requirement: Option[Set[Int]])
  extends SubReceiver {
  def receive(message: Message, context: DispatchContext) { message match {
    case Perform =>
      (requirement, selections.count(x => x.selectable)) match {
        case (_, 0) =>
          player.popSub()
          context.send(player, NothingToSelect)
        case (Some(requireCounts), maxCount) if maxCount < requireCounts.min =>
          player.popSub()
          context.send(player, NothingToSelect)
        case _ =>
          val dialogMessage = new StringBuilder()
          appendSelectionDialog(dialogMessage)
          context.send(context, InputRequired(dialogMessage.toString(), player))
      }
    case InputArrived(input) =>
      val selectedIndexes = input.split(',').map(x => x.trim).filter(x => !x.isEmpty).map(x => x.toInt).distinct.intersect(
        selections.zipWithIndex.filter(x => x._1.selectable).map(x => x._2))

      def reply() {
        player.popSub()
        selectedIndexes.length match {
          case 0 => context.send(player, SelectedNothing)
          case 1 => context.send(player, SelectedOne(selectedIndexes.head))
          case _ => context.send(player, SelectedMulti(selectedIndexes.toVector))
        }
      }

      (requirement, selectedIndexes.length) match {
        case (None, _) => reply()
        case (Some(requireCounts), actualCount) if requireCounts(actualCount) => reply()
        case (Some(requireCounts), actualCount) if !requireCounts(actualCount) =>
          val dialogMessage = new StringBuilder("you do not make the required number of selection" + Utils.newline)
          appendSelectionDialog(dialogMessage)
          context.send(context, InputRequired(dialogMessage.toString(), player))
      }
  } }

  private[this] def appendSelectionDialog(builder: StringBuilder) {
    builder.append(player.name + ": " + dialogTitle + Utils.newline)
    selections.zipWithIndex.filter(x => x._1.selectable).foreach { x =>
      builder.append(x._2)
      builder.append(": ")
      builder.append(x._1.title)
      builder.append(Utils.newline)
    }
  }
}

class Selection(val title: String, val selectable: Boolean)
