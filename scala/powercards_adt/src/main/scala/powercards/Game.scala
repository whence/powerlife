package powercards

object Stage extends Enumeration {
  type Stage = Value
  val Action, Treasure, Buy, Cleanup = Value
}

class Game(playerNames: Seq[String]) {
  var players: Vector[Player] = playerNames.map(new Player(_)).toVector
  private var activePlayerIndex = util.Random.nextInt(players.length)

  val piles: Vector[Pile] = {
    import Cards._
    val treasures = Seq(
      new Pile(copper, 60 - players.length * 7),
      new Pile(silver, 40),
      new Pile(gold, 30))
    val victories = {
      val n = players.length match {
        case 2 => 8
        case 3 | 4 => 12
      }
      Seq(estate, duchy, province).map(new Pile(_, n))
    }
    val kingdoms = Seq(remodel, garden, festival, smithy, throneRoom).map(new Pile(_, 10))
    (treasures ++ victories ++ kingdoms).toVector
  }

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
            activePlayer.played :+= card
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
          activePlayer.played ++= cards
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
            activePlayer.discard :+= card
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
      activePlayer.discard ++= activePlayer.hand ++ activePlayer.played
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

  def isEnded: Boolean = piles.find(_.sample == Cards.province).get.isEmpty || piles.count(_.isEmpty) >= 3

  def playTilEnd(implicit io: IO): Unit = {
    def play(): Option[Seq[EndStat]] = {
      playOne(io)
      if (isEnded) Some(endStats) else None
    }
    for (stat <- Utils.loopTil(play)) {
      io.output(s"${stat.player.name}: ${stat.vps} ${if (stat.won) "(won)" else ""}")
    }
  }

  def endStats: Seq[EndStat] = {
    def playerToVps(player: Player): Int = {
      def cardToVps(card: Card): Int = card.feature match {
        case BasicAction(_) | SelfTrashAction(_) | BasicTreasure(_) => 0
        case BasicVictory(vps) => vps
        case DynamicVictory(f) => f(player)
      }
      Seq(player.deck, player.hand, player.played, player.discard).flatten.map(cardToVps).sum
    }
    val playerVps = players.map(p => (p, playerToVps(p)))
    val maxVps = playerVps.map(_._2).max
    playerVps.map { case (player, vps) => new EndStat(player, vps, vps == maxVps) }.sortBy(_.vps)(Ordering[Int].reverse)
  }
}

class EndStat(val player: Player, val vps: Int, val won: Boolean)

class Player(val name: String) {
  private val fullInitDeck = util.Random.shuffle(
    Seq.fill(3)(Cards.estate) ++ Seq.fill(7)(Cards.copper))
  var deck: Vector[Card] = fullInitDeck.drop(5).toVector
  var hand: Vector[Card] = fullInitDeck.take(5).toVector
  var played: Vector[Card] = Vector.empty
  var discard: Vector[Card] = Vector.empty

  def drawCards(n: Int): Vector[Card] = {
    def draw(n: Int): Vector[Card] = {
      val (newDeck, drawn) = deck.splitAt(deck.length - n)
      deck = newDeck
      val cards = drawn.reverse
      hand ++= cards
      cards
    }

    @annotation.tailrec
    def loop(acc: Vector[Card]): Vector[Card] = {
      if (deck.length >= n - acc.length) {
        acc ++ draw(n - acc.length)
      } else if (deck.nonEmpty) {
        loop(acc ++ draw(deck.length))
      } else if (discard.nonEmpty) {
        deck = util.Random.shuffle(discard)
        discard = Vector.empty
        loop(acc)
      } else {
        acc
      }
    }

    loop(Vector.empty)
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

  private def gainCard(io: IO, game: Game, player: Player, predicate: Pile => Boolean): Option[Card] = {
    import Dialog._
    choose(io, MandatoryOne, "Select a pile to gain",
      items = game.piles.map(Item.fromPile(predicate))) match {
      case NonSelectable =>
        io.output("No pile available to gain")
        None
      case Index(index) =>
        val pile = game.piles(index)
        val card = pile.pop()
        player.discard :+= card
        io.output(s"Gained $card")
        Some(card)
      case Indexes(_) | Skip =>
        assert(assertion = false)
        None
    }
  }

  private def trashCard(io: IO, game: Game, player: Player): Option[Card] = {
    import Dialog._
    choose(io, MandatoryOne, "Select a card to trash",
      items = player.hand.map(Item.fromCard(Fn.const(true)))) match {
      case NonSelectable =>
        io.output("No card to trash")
        None
      case Index(index) =>
        val (card, hand) = Utils.divide(player.hand, index)
        player.hand = hand
        game.trash :+= card
        io.output(s"Trashed $card")
        Some(card)
      case Indexes(_) | Skip =>
        assert(assertion = false)
        None
    }
  }

  val garden = new Card("Garden", cost = 4, feature = DynamicVictory { player =>
    Seq(player.deck, player.hand, player.played, player.discard).map(_.length).sum / 10
  })

  val festival = new Card("Festival", cost = 4, feature = SelfTrashAction { (io, game, card, trashed) =>
    if (!trashed) {
      game.trash :+= card
      io.output(s"Trashed $card")
      gainCard(io, game, game.activePlayer, Fn.all(Seq({ !_.isEmpty }, { _.sample.cost <= 5 })))
      true
    } else false
  })

  val smithy = new Card("Smithy", cost = 4, feature = BasicAction { (io, game) =>
    game.activePlayer.drawCards(3)
  })

  val remodel = new Card("Remodel", cost = 4, feature = BasicAction { (io, game) =>
    for (trashedCard <- trashCard(io, game, game.activePlayer)) {
      gainCard(io, game, game.activePlayer, Fn.all(Seq({ !_.isEmpty }, { _.sample.cost <= trashedCard.cost + 2 })))
    }
  })

  val throneRoom = new Card("Throne Room", cost = 4, feature = BasicAction { (io, game) =>
    import Dialog._
    choose(io, MandatoryOne, "Select an action card to play twice",
      items = game.activePlayer.hand.map(Item.fromCard(_.feature.isAction))) match {
      case NonSelectable =>
        io.output("No action card to play")
      case Index(index) =>
        val (card, hand) = Utils.divide(game.activePlayer.hand, index)
        game.activePlayer.hand = hand
        game.activePlayer.played :+= card
        card.feature match {
          case BasicAction(play) =>
            io.output(s"Playing $card first time")
            play(io, game)
            io.output(s"Playing $card second time")
            play(io, game)
          case SelfTrashAction(play) =>
            io.output(s"Playing $card first time")
            val trashed = play(io, game, card, false)
            io.output(s"Playing $card second time")
            play(io, game, card, trashed)
          case BasicTreasure(_) | BasicVictory(_) | DynamicVictory(_) => assert(assertion = false)
        }
      case Indexes(_) | Skip => assert(assertion = false)
    }
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
  def const[A, B](x: B): A => B = { _ => x }
}