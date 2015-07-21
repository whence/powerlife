package powercards

import scala.collection.mutable

trait Interactive {
  def chooseOne(message: String, items: IndexedSeq[Item]): choices.one.Choice
  def chooseOptionalOne(message: String, items: IndexedSeq[Item]): choices.optional_one.Choice
  def chooseOptionalBulkable(message: String, items: IndexedSeq[BulkableItem]): choices.optional_bulkable.Choice
}

trait ConsoleInteractive extends Interactive {
  private def input(): String = io.StdIn.readLine()

  def chooseOne(message: String, items: IndexedSeq[Item]): choices.one.Choice = {
    println(message)
    choices.one.One(2)
  }
  def chooseOptionalOne(message: String, items: IndexedSeq[Item]): choices.optional_one.Choice = ???
  def chooseOptionalBulkable(message: String, items: IndexedSeq[BulkableItem]): choices.optional_bulkable.Choice = ???
}

trait ReplayableInteractive extends Interactive {
  def inputQueue: mutable.Queue[String]
  def throwOnEmptyQueue: Boolean

  private val onePatten = """ONE (\d+)""".r

  abstract override def chooseOne(message: String, items: IndexedSeq[Item]): choices.one.Choice = {
    if (throwOnEmptyQueue) {
      import Functions._
      requireState(inputQueue.nonEmpty)
    }
    if (inputQueue.isEmpty) {
      super.chooseOne(message, items)
    } else {
      inputQueue.dequeue() match {
        case "NonSelectable" =>
          choices.one.NonSelectable
        case onePatten(index) =>
          choices.one.One(index.toInt)
      }
    }
  }

  abstract override def chooseOptionalOne(message: String, items: IndexedSeq[Item]): choices.optional_one.Choice = ???
  abstract override def chooseOptionalBulkable(message: String, items: IndexedSeq[BulkableItem]): choices.optional_bulkable.Choice = ???
}

class Item(val name: String, val selectable: Boolean)
class BulkableItem(name: String, selectable: Boolean, val bulkable: Boolean) extends Item(name, selectable)
