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

class Pile(val sample: Card, initialSize: Int)

class Card(val name: String, val cost: Int, val feature: CardFeature)

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
case class SelfTrashAction(play: (IO, Game, Boolean) => Boolean) extends CardFeature {
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

  def choose(io: IO, requirement: Requirement, message: String, items: Vector[(String, Boolean)]): Choice = {
    def selectOne(input: String): Option[Choice] = {
      val index = io.input().toInt
      items(index) match {
        case (_, true) => Some(Index(index))
        case (name, false) =>
          io.output(s"$name is not selectable")
          None
      }
    }

    def selectMany(input: String): Option[Choice] = {
      val indexes = input.split(',').map(_.trim).withFilter(_.nonEmpty).map(_.toInt).sorted
      val nonSelectable = indexes.map(items(_)).withFilter(!_._2).map(_._1)
      if (nonSelectable.nonEmpty) {
        io.output(s"${nonSelectable.mkString(", ")} are not selectable")
        None
      } else Some(Indexes(indexes.toVector))
    }

    def ask(): Option[Choice] = {
      io.output(message)
      for (((name, selectable), i) <- items.zipWithIndex) {
        io.output(s"[$i] $name ${if (selectable) "(select)" else ""}")
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
          ???
      }
    }

    if (items.exists(_._2)) {
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