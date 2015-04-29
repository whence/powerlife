package powercards

object Stage extends Enumeration {
  type Stage = Value
  val Action, Treasure, Buy, Cleanup = Value
}

class Game(playerNames: Seq[String]) {
  var players: Vector[Player] = playerNames.map(new Player(_)).toVector
  private var activePlayerIndex = util.Random.nextInt(players.size)
  val piles: Vector[Pile] = Vector(
    new Pile(Cards.copper, 60), new Pile(Cards.estate, 12),
    new Pile(Cards.remodel, 10))
  var trash: Vector[Card] = Vector.empty
  var stage: Stage.Stage = Stage.Action
  var actions: Int = 1
  var buys: Int = 1
  var coins: Int = 0

  def activePlayer = players(activePlayerIndex)

  def activateNextPlayer(): Unit = {
    activePlayerIndex += 1
    if (activePlayerIndex >= players.size) {
      activePlayerIndex = 0
    }
  }
}

class Player(val name: String) {
  private val fullInitDeck = util.Random.shuffle(
    Seq.fill(3)(Cards.estate) ++ Seq.fill(7)(Cards.copper))
  var deck: Vector[Card] = fullInitDeck.drop(5).toVector
  var hand: Vector[Card] = fullInitDeck.take(5).toVector
  var played: Vector[Card] = Vector.empty
  var discard: Vector[Card] = Vector.empty
}

class Pile(val sample: Card, var size: Int)

sealed abstract class Card {
  def name: String
  def cost: Int
}
case class BasicActionCard(name: String, cost: Int, play: (Game, IO) => Unit) extends Card
case class SelfTrashActionCard(name: String, cost: Int, play: (Game, IO, Boolean) => Boolean) extends Card
case class BasicTreasureCard(name: String, cost: Int, coins: Int) extends Card
case class BasicVictoryCard(name: String, cost: Int, vps: Int) extends Card

trait IO {
  def input(): String
  def output(message: String): Unit
}

object ConsoleIO extends IO {
  def input(): String = io.StdIn.readLine()
  def output(message: String) = println(message)
}

class RecordedIO(inputs: Seq[String]) extends IO {
  val inputStack = collection.mutable.Stack.concat(inputs)
  val outputBuffer = collection.mutable.ArrayBuffer.empty[String]

  def input(): String = inputStack.pop()
  def output(message: String) = outputBuffer.append(message)
}

object Cards {
  def isAction(card: Card): Boolean = card match {
    case BasicActionCard(_, _, _) => true
    case SelfTrashActionCard(_, _, _) => true
    case BasicTreasureCard(_, _, _) => false
    case BasicVictoryCard(_, _, _) => false
  }

  def isTreasure(card: Card): Boolean = card match {
    case BasicActionCard(_, _, _) => false
    case SelfTrashActionCard(_, _, _) => false
    case BasicTreasureCard(_, _, _) => true
    case BasicVictoryCard(_, _, _) => false
  }

  def isVictory(card: Card): Boolean = card match {
    case BasicActionCard(_, _, _) => false
    case SelfTrashActionCard(_, _, _) => false
    case BasicTreasureCard(_, _, _) => false
    case BasicVictoryCard(_, _, _) => true
  }

  val copper = BasicTreasureCard("Copper", cost = 0, coins = 1)
  val silver = BasicTreasureCard("Silver", cost = 3, coins = 2)
  val gold = BasicTreasureCard("Gold", cost = 6, coins = 3)

  val estate = BasicVictoryCard("Estate", cost = 2, vps = 1)
  val duchy = BasicVictoryCard("Duchy", cost = 5, vps = 3)
  val province = BasicVictoryCard("Province", cost = 8, vps = 6)

  val remodel = BasicActionCard("Remodel", cost = 4, (game, io) => {
  })
}

object Dialog {
  sealed abstract class Requirement
  case object Unlimited
  case object MandatoryOne
  case object OptionalOne

  sealed abstract class Choice
  case object NonSelectable
  case object Skip
  case class Indexes(indexes: Vector[Int])
}
