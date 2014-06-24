package core

class Game extends Receiver {
  var players = Vector.empty[Receiver]
  var activePlayerIndex = 0
  val trash = new Zone[Card]
  var stage = TurnStage.Action
  var stat = TurnStat(actions = 1, buys = 1, coins = 0)

  def receive(message: Message, context: DispatchContext) { (stage, stat, message) match {
    case (TurnStage.Action, _, StartGame(joiningPlayers)) =>
      players = joiningPlayers
      activePlayerIndex = util.Random.nextInt(players.length)
      context.send(this, Progress)
    case (TurnStage.Action, TurnStat(0, _, _), Progress) =>
      context.send(this, NextStage)
    case (TurnStage.Action, _, Progress) =>
      context.send(activePlayer, PlayAction)
    case (TurnStage.Action, _, NextStage) =>
      stat = stat.copy(actions = 0)
      stage = TurnStage.Treasure
      context.send(this, Progress)

    case (TurnStage.Treasure, _, Progress) =>
      context.send(activePlayer, PlayTreasure)
    case (TurnStage.Treasure, _, NextStage) =>
      stage = TurnStage.Buy
      context.send(this, Progress)

    case (TurnStage.Buy, TurnStat(_, 0, _), Progress) =>
      context.send(this, NextStage)
    case (TurnStage.Buy, _, Progress) =>
    case (TurnStage.Buy, _, NextStage) =>
      stage = TurnStage.Cleanup
      context.send(this, Progress)

    case (TurnStage.Cleanup, _, Progress) =>
      context.send(activePlayer, Cleanup)
    case (TurnStage.Cleanup, _, NextStage) =>
      activePlayerIndex += 1
      if (activePlayerIndex >= players.size) {
        activePlayerIndex = 0
      }
      stat = TurnStat(actions = 1, buys = 1, coins = 0)
      stage = TurnStage.Action
      context.send(this, Progress)

    case (_, _, ackee @ StatChange(delta, sender)) =>
      stat = stat plus delta
      context.send(sender, Ack(ackee))
    case (_, _, ackee @ PostCards(cards, CardZoneName.Trash, sender)) =>
      trash.appendAll(cards)
      context.send(sender, Ack(ackee))
  }}

  def activePlayer: Receiver = players(activePlayerIndex)
}

object TurnStage extends Enumeration {
  val Action, Treasure, Buy, Cleanup = Value
}

object CardZoneName extends Enumeration {
  val Deck, Hand, Played, Discard, Trash = Value
}

case class TurnStat(actions: Int = 0, buys: Int = 0, coins: Int = 0) {
  def plus(delta: TurnStat): TurnStat = {
    TurnStat(
      actions = actions + delta.actions,
      buys = buys + delta.buys,
      coins = coins + delta.coins
    )
  }
}