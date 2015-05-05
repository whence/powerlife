package powercards

object Stage extends Enumeration {
  type Stage = Value
  val Action, Treasure, Buy, Cleanup = Value
}

class Game(playerNames: Seq[String]) {
  var players: Vector[Player] = playerNames.map(new Player(_)).toVector
  private var activePlayerIndex = util.Random.nextInt(players.length)
  val piles: Vector[Pile] = Vector(
    new Pile(Cards.copper, 60), new Pile(Cards.estate, 12),
    new Pile(Cards.remodel, 10))
  var trash: Vector[Card] = Vector.empty
  var stage: Stage.Stage = Stage.Action
  var actions: Int = 1
  var buys: Int = 1
  var coins: Int = 0

  def activePlayer = players(activePlayerIndex)

  def playOne(implicit io: IO): Unit = {
    import Dialog._
    import Stage._

    def playAction(): Unit = {
      def skip(): Unit = {
        actions = 0
        stage = Treasure
      }
      if (actions > 0) {
        choose(io, OptionalOne, "Select an action card to play",
          items = activePlayer.hand.map(Item.fromCard(_.feature.isAction))) match {
          case NonSelectable =>
            io.output("No action card to play. Skip to treasure stage")
            skip()
          case Skip =>
            io.output("Skip to treasure stage")
            skip()
          case Index(index) =>
            actions -= 1
            val (card, hand) = Utils.divide(activePlayer.hand, index)
            activePlayer.hand = hand
            activePlayer.played = activePlayer.played :+ card
            io.output(s"Playing $card")
            card.feature match {
              case BasicAction(play) => play(io, this)
              case SelfTrashAction(play) => play(io, this, card, false)
              case BasicTreasure(_) | BasicVictory(_) | DynamicVictory(_) => assert(assertion = false)
            }
          case Indexes(_) => assert(assertion = false)
        }
      } else {
        io.output("No action point. Skip to treasure stage")
        skip()
      }
    }

    def playTreasure(): Unit = {
      def skip(): Unit = {
        buys = 0
        stage = Buy
      }
      choose(io, Unlimited, "Select treasure cards to play",
        items = activePlayer.hand.map(Item.fromCard(_.feature.isTreasure))) match {
        case NonSelectable =>
          io.output("No treasure card to play. Skip to buy stage")
          skip()
        case Skip =>
          io.output("Skip to buy stage")
          skip()
        case Indexes(indexes) =>
          val (cards, hand) = Utils.divides(activePlayer.hand, indexes)
          activePlayer.hand = hand
          activePlayer.played = activePlayer.played ++ cards
          io.output(s"Playing ${cards.mkString(", ")}")
          for (card <- cards) {
            card.feature match {
              case BasicAction(_) | SelfTrashAction(_) | BasicVictory(_) | DynamicVictory(_) => assert(assertion = false)
              case BasicTreasure(c) => coins += c
            }
          }
        case Index(_) => assert(assertion = false)
      }
    }

    def playBuy(): Unit = {
      def skip(): Unit = {
        coins = 0
        stage = Cleanup
      }

      if (buys > 0) {
        choose(io, OptionalOne, "Select a pile to buy",
          items = piles.map(Item.fromPile(Fn.all(Seq({ !_.isEmpty }, { _.sample.cost <= coins }))))) match {
          case NonSelectable =>
            io.output("No card to buy. Skip to cleanup stage")
            skip()
          case Skip =>
            io.output("Skip to cleanup stage")
            skip()
          case Index(index) =>
            buys -= 1
            val pile = piles(index)
            val card = pile.pop()
            activePlayer.discard = activePlayer.discard :+ card
            coins -= card.cost
            io.output(s"Bought $card")
          case Indexes(_) => assert(assertion = false)
        }
      } else {
        io.output("No more buys. Skip to cleanup stage")
        skip()
      }
    }

    def playCleanup(): Unit = {
      activePlayer.discard = activePlayer.discard ++ activePlayer.hand ++ activePlayer.played
      activePlayer.played = Vector.empty
      activePlayer.hand = Vector.empty
      activePlayer.drawCards(5)
      activePlayerIndex = if (activePlayerIndex == players.length - 1) 0 else activePlayerIndex + 1
      stage = Action
      actions = 1
      buys = 1
      coins = 0
    }

    stage match {
      case Action => playAction()
      case Treasure => playTreasure()
      case Buy => playBuy()
      case Cleanup => playCleanup()
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

  def drawCards(n: Int): Vector[Card] = {
    ???
  }
}

class Pile(val sample: Card, initialSize: Int) {
  var size = initialSize

  def isEmpty: Boolean = size == 0

  def pop(): Card = {
    size -= 1
    sample
  }
}

class Card(val name: String, val cost: Int, val feature: CardFeature) {
  override def toString = name
}

sealed abstract class CardFeature {
  def isAction: Boolean
  def isTreasure: Boolean
  def isVictory: Boolean
}
case class BasicAction(play: (IO, Game) => Unit) extends CardFeature {
  val isAction = true
  val isTreasure = false
  val isVictory = false
}
case class SelfTrashAction(play: (IO, Game, Card, Boolean) => Boolean) extends CardFeature {
  val isAction = true
  val isTreasure = false
  val isVictory = false
}
case class BasicTreasure(coins: Int) extends CardFeature {
  val isAction = false
  val isTreasure = true
  val isVictory = false
}
case class BasicVictory(vps: Int) extends CardFeature {
  val isAction = false
  val isTreasure = false
  val isVictory = true
}
case class DynamicVictory(vps: Player => Int) extends CardFeature {
  val isAction = false
  val isTreasure = false
  val isVictory = true
}

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
  val copper = new Card("Copper", cost = 0, feature = BasicTreasure(coins = 1))
  val silver = new Card("Silver", cost = 3, feature = BasicTreasure(coins = 2))
  val gold = new Card("Gold", cost = 6, feature = BasicTreasure(coins = 3))

  val estate = new Card("Estate", cost = 2, feature = BasicVictory(vps = 1))
  val duchy = new Card("Duchy", cost = 5, feature = BasicVictory(vps = 3))
  val province = new Card("Province", cost = 8, feature = BasicVictory(vps = 6))

  val remodel = new Card("Remodel", cost = 4, feature = BasicAction { (io, game) =>
  })
}

object Dialog {
  sealed abstract class Requirement
  case object Unlimited extends Requirement
  case object MandatoryOne extends Requirement
  case object OptionalOne extends Requirement

  sealed abstract class Choice
  case object NonSelectable extends Choice
  case object Skip extends Choice
  case class Index(index: Int) extends Choice
  case class Indexes(indexes: Vector[Int]) extends Choice

  class Item(val name: String, val selectable: Boolean)
  object Item {
    def fromCard(pred: Card => Boolean)(card: Card): Item = new Item(card.name, pred(card))
    def fromPile(pred: Pile => Boolean)(pile: Pile): Item = new Item(pile.sample.name, pred(pile))
  }

  def choose(io: IO, requirement: Requirement, message: String, items: Vector[Item]): Choice = {
    def selectOne(input: String): Option[Choice] = {
      val index = io.input().toInt
      val item = items(index)
      if (item.selectable) {
        Some(Index(index))
      } else {
        io.output(s"${item.name} is not selectable")
        None
      }
    }

    def selectMany(input: String): Option[Choice] = {
      val indexes = input.split(',').map(_.trim).withFilter(_.nonEmpty).map(_.toInt).sorted
      val nonSelectable = indexes.map(items(_)).withFilter(!_.selectable).map(_.name)
      if (nonSelectable.nonEmpty) {
        io.output(s"${nonSelectable.mkString(", ")} are not selectable")
        None
      } else Some(Indexes(indexes.toVector))
    }

    def ask(): Option[Choice] = {
      io.output(message)
      for ((item, i) <- items.zipWithIndex) {
        io.output(s"[$i] ${item.name} ${if (item.selectable) "(select)" else ""}")
      }
      requirement match {
        case MandatoryOne =>
          selectOne(io.input())
        case OptionalOne =>
          io.output("or skip")
          io.input() match {
            case "skip" => Some(Skip)
            case input => selectOne(input)
          }
        case Unlimited =>
          io.output("or all, or skip")
          io.input() match {
            case "skip" => Some(Skip)
            case "all" =>
              val indexes = items.zipWithIndex.withFilter(_._1.selectable).map(_._2)
              Some(Indexes(indexes))
            case input => selectMany(input)
          }
      }
    }

    if (items.exists(_.selectable)) {
      Utils.loopTil(ask)
    } else {
      NonSelectable
    }
  }
}

object Utils {
  def divides[A](items: Vector[A], indexes: Vector[Int]): (Vector[A], Vector[A]) = indexes match {
    case Seq() =>
      (Vector.empty, items)
    case Seq(i) =>
      val (x, y) = divide(items, i)
      (Vector(x), y)
    case _ =>
      val selected = indexes.map(items(_))
      val unselected = Range(0, items.length).diff(indexes).map(items(_)).toVector
      (selected, unselected)
  }

  def divide[A](items: Vector[A], index: Int): (A, Vector[A]) = {
    val selected = items(index)
    val unselected = items.patch(index, Nil, 1)
    (selected, unselected)
  }

  @annotation.tailrec
  def loopTil[A](f: () => Option[A]): A = f() match {
    case Some(x) => x
    case None => loopTil(f)
  }
}

object Fn {
  def all[A](predicates: Seq[A => Boolean]): A => Boolean = { x => predicates.forall(_(x)) }
  def non[A](predicate: A => Boolean): A => Boolean = { x => !predicate(x) }
}