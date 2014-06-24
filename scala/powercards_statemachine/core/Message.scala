package core

trait Message

case object Progress extends Message
case object Perform extends Message

case class Ack(message: Message) extends Message

case class StartGame(players: Vector[Receiver]) extends Message

case class StatChange(stat: TurnStat, sender: Receiver) extends Message

case object NextStage extends Message

case object PlayAction extends Message
case class ActionCardPlayed(card: Card with Actionable) extends Message

case object PlayTreasure extends Message

case object Cleanup extends Message

case class InputRequired(dialogText: String, sender: Receiver) extends Message
case class InputArrived(input: String) extends Message

case object NothingToSelect extends Message
case object SelectedNothing extends Message
case class SelectedOne(index: Int) extends Message
case class SelectedMulti(indexes: Vector[Int]) extends Message

case object TrashedNothing extends Message
case class TrashedOne(card: Card) extends Message

case object GainedNothing extends Message
case class GainedOne(card: Card) extends Message

case class PostCards(card: Vector[Card], destination: CardZoneName.Value, sender: Receiver) extends Message