package core

object Player {
  def apply(name: String, game: Receiver): Player = {
    val player = new Player(name, game)
    val deck = util.Random.shuffle(Seq.fill(3)(Estate) ++ Seq.fill(7)(Copper))
    player.hand.appendAll(deck.take(5))
    player.deck.appendAll(deck.drop(5))
    player.hand.appendAll(Seq(Remodel, ThroneRoom))
    player
  }
}

class Player private (val name: String, val game: Receiver)
  extends Receiver with StackContainer[SubReceiver] {
  val deck = new Zone[Card]
  val hand = new Zone[Card]
  val played = new Zone[Card]
  val discard = new Zone[Card]

  def receive(message: Message, context: DispatchContext) { message match {
    case PlayAction if noSub =>
      pushSub(new ActionStagePlayer(this))
      context.send(this, Perform)
    case PlayTreasure if noSub =>
      pushSub(new TreasureStagePlayer(this))
      context.send(this, Perform)
    case NextStage | Progress if noSub =>
      context.send(game, message)
    case Cleanup if noSub =>
      cleanup()
      context.send(game, NextStage)
    case unknown if !noSub =>
      topSub.receive(unknown, context)
  }}

  @annotation.tailrec
  final def drawCards(count: Int): Int = {
    if (count > 0) {
      if (deck.isEmpty) {
        if (discard.isEmpty)
          count
        else {
          discard.shuffle()
          discard.transferAllToEnd(deck)
          drawCards(count)
        }
      } else {
        deck.transferToEnd(0, hand)
        drawCards(count - 1)
      }
    } else count
  }

  def cleanup() {
    hand.transferAllToEnd(discard)
    played.transferAllToEnd(discard)
    drawCards(5)
  }
}