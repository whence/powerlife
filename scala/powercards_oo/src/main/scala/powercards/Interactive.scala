package powercards

trait Interactive {
  def io: IO
  def chooseOne(message: String, items: IndexedSeq[Item]): choices.one.Choice = ???
  def chooseOptionalOne(message: String, items: IndexedSeq[Item]): choices.optional_one.Choice = ???
  def chooseOptionalBulkable(message: String, items: IndexedSeq[BulkableItem]): choices.optional_bulkable.Choice = ???
}

class Item(val name: String, val selectable: Boolean)
class BulkableItem(name: String, selectable: Boolean, val bulkable: Boolean) extends Item(name, selectable)
