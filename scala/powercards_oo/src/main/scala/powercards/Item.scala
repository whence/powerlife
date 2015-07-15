package powercards

class Item(val name: String, val selectable: Boolean)

object Item {
  def fromCard(pred: Card => Boolean)(card: Card): Item = new Item(card.name, pred(card))
  def fromPile(pred: Pile => Boolean)(pile: Pile): Item = new Item(pile.sample.name, pred(pile))
}
