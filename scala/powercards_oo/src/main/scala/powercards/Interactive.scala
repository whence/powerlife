package powercards

trait Interactive {
  def io: IO
  def chooseOne(message: String, items: IndexedSeq[Item]): ChoiceOne = ???
  def chooseOptionalOne(message: String, items: IndexedSeq[Item]): ChoiceOptionalOne = ???
}

case class Item(name: String, selectable: Boolean)

sealed abstract class ChoiceOne
case object NonSelectableOne extends ChoiceOne
case class IndexOne(index: Int) extends ChoiceOne

sealed abstract class ChoiceOptionalOne
case object NonSelectableOptionalOne extends ChoiceOptionalOne
case object SkipOptionalOne extends ChoiceOptionalOne
case class IndexOptionalOne(index: Int) extends ChoiceOptionalOne
