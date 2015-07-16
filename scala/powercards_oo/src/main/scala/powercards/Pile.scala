package powercards

import scala.collection.mutable

class Pile(cardFactory: () => Card, initialSize: Int) {
  val sample: Card = cardFactory()
  var size: Int = initialSize
  private val buffer = new mutable.Stack[Card]

  def isEmpty: Boolean = size <= 0

  def push(card: Card): Unit = {
    import Functions._
    require(card.getClass.isAssignableFrom(sample.getClass))
    buffer.push(card)
    size += 1
  }

  def pop(): Card = {
    import Functions._
    requireState(!isEmpty)
    val card = if (buffer.isEmpty) cardFactory() else buffer.pop()
    size -= 1
    card
  }
}
