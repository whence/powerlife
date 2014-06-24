package core

object Player {
  def apply(name: String)(implicit cardlib: CardLibrary): Player = {
    val deck = util.Random.shuffle(Seq.fill(3)(cardlib.estate) ++ Seq.fill(7)(cardlib.copper))
    new Player(name,
      deck = deck.drop(5).toVector,
      hand = deck.take(5).toVector ++ Vector(cardlib.remodel, cardlib.throneRoom),
      played = Vector.empty[Card],
      discard = Vector.empty[Card])
  }
}

case class Player(name: String,
  deck: Vector[Card],
  hand: Vector[Card],
  played: Vector[Card],
  discard: Vector[Card]) {

  @annotation.tailrec
  final def drawCards(count: Int): Player = {
    (count, deck.isEmpty, discard.isEmpty) match {
      case (0, _, _) => this
      case (_, true, true) => this
      case (_, true, false) => copy(
        deck = util.Random.shuffle(discard),
        discard = Vector.empty).drawCards(count)
      case _ => copy(
        deck = deck.tail,
        hand = hand :+ deck.head)
        .drawCards(count - 1)
    }
  }

  def cleanup: Player = {
    copy(
      hand = Vector.empty[Card],
      played = Vector.empty[Card],
      discard = played ++ hand ++ discard)
      .drawCards(5)
  }
}