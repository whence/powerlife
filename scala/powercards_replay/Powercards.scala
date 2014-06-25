package core

import collection.immutable.Queue

object Powercards {
  
  class RestoreContext(
    var checkpoint: Game,
    var pendingUserInputs: Queue[Vector[Int]],
    var processedUserInputs: Queue[Vector[Int]]
  ) {
    def ask[A](player: Player, message: String, choices: Vector[A], selectable: A => Boolean)
      : (Vector[A], Vector[A]) =
      if (!this.pendingUserInputs.isEmpty) {
        val (input, _) = this.pendingUserInputs.dequeue
        val inputset = input.toSet
        val (selected, nonselected) = choices.zipWithIndex.partition { x => inputset(x._2) }
        (selected.map { _._1 }, nonselected.map { _._1 })
      } else
        throw pause(player, message, choices)
    
    def pause[A](player: Player, message: String, choices: Vector[A]): AskException = 
      new AskException(new AskDialog(player, message, choices.map(_.toString)))
    
    def approve() {
      val (input, otherInputs) = this.pendingUserInputs.dequeue
      this.processedUserInputs = this.processedUserInputs.enqueue(input)
      this.pendingUserInputs = otherInputs
    }
    
    def markCheckpoint(game: Game) {
      this.checkpoint = new Game(
          players = game.players.map(p => new Player(
            name = p.name, 
            stat = new PlayerStat(actions = p.stat.actions, buys = p.stat.buys, coins = p.stat.coins),
            stage = p.stage,
            deck = p.deck,
            hand = p.hand,
            played = p.played,
            discard = p.discard
          )), 
          activePlayerIndex = game.activePlayerIndex,
          trash = game.trash
        )
      this.processedUserInputs = Queue.empty
    }
  } 

  class Game(
    val players: Vector[Player],
    var activePlayerIndex: Int,
    var trash: Vector[Card]
  ) {
    def activePlayer: Player = this.players(this.activePlayerIndex)
    
    def progress(userInputs: Queue[Vector[Int]]): (AskDialog, RestoreContext) = {
      
      @annotation.tailrec
      def loop(context: RestoreContext) {
        context.markCheckpoint(this)
        this.activePlayer.stage match {
          case PlayerStage.Action => 
            val actionCards = this.activePlayer.hand collect { case card: Actionable => card }
            if (!actionCards.isEmpty) {
              val cards = context.ask(this.activePlayer, "choose an action card to play", actionCards)
              if (cards.isEmpty) {
                context.approve()
                this.activePlayer.stage = PlayerStage.Treasure
                loop(context)
              } else if (cards.length == 1) {
                context.approve()
                val actionCard = cards(0)
                moveCard(actionCard, this.activePlayer.hand, this.activePlayer.played) { (from, to) => 
                  this.activePlayer.hand = from
                  this.activePlayer.played = to
                }
                actionCard.play(this, context)
                loop(context)
              } else
                throw context.pause(this.activePlayer, "you can only choose one action card to play", actionCards)
            } else {
              this.activePlayer.stage = PlayerStage.Treasure
              loop(context)
            }
          case PlayerStage.Treasure => 
            val treasureCards = this.activePlayer.hand collect { case card: Treasurable => card }
            if (!treasureCards.isEmpty) {
              val cards = context.ask(this.activePlayer, "choose treasure cards to play", treasureCards)
              if (cards.isEmpty) {
                context.approve()
                this.activePlayer.stage = PlayerStage.Buy
                loop(context)
              } else {
                context.approve()
                moveCards(cards, this.activePlayer.hand, this.activePlayer.played) { (from, to) => 
                  this.activePlayer.hand = from
                  this.activePlayer.played = to
                }
                this.activePlayer.stat.coins += cards.map(_.coins).sum
                loop(context)
              }
            } else {
              this.activePlayer.stage = PlayerStage.Buy
              loop(context)
            }
        }
      }
      
      val context = new RestoreContext(
        checkpoint = null,
        pendingUserInputs = userInputs,
        processedUserInputs = Queue.empty
      ) 
      
      try {
        loop(context)
        throw new Exception("game loop is incomplete")
      } catch {
        case ex: AskException => (ex.dialog, context)
      }
    }
    
    def moveCard(card: Card, from: Vector[Card], to: Vector[Card])
      (assignment: (Vector[Card], Vector[Card]) => Unit) {
      val from1 = from.filter { _ != card }
      val to1 = to :+ card
      assignment(from1, to1)
    }
    
    def moveCards(cards: Vector[Card], from: Vector[Card], to: Vector[Card])
      (assignment: (Vector[Card], Vector[Card]) => Unit) {
      val from1 = from.filter { !cards.toSet(_) }
      val to1 = to ++ cards
      assignment(from1, to1)
    }
  }
  
  class Player(
    val name: String,
    val stat: PlayerStat,
    var stage: PlayerStage.Value,
    var deck: Vector[Card],
    var hand: Vector[Card],
    var played: Vector[Card],
    var discard: Vector[Card]
  ) {
    
    def activate() {
      this.stat.actions = 1
      this.stat.buys = 1
      this.stat.coins = 0
      this.stage = PlayerStage.Action
    }
    
    @annotation.tailrec
    final def drawCards(count: Int): Boolean = {
      if (count == 0) true
      else if (this.deck.isEmpty && this.discard.isEmpty) false
      else if (this.deck.isEmpty) {
        this.deck = util.Random.shuffle(this.discard)
        this.discard = Vector.empty
        drawCards(count)
      }
      else {
        this.hand :+= this.deck.last
        this.deck = this.deck.dropRight(1)
        drawCards(count - 1)
      }
    }
    
    override def toString: String = {
      val builder = new collection.mutable.StringBuilder
      builder.append("name %s stat %s stage %s \n".format(this.name, this.stat, this.stage))
      builder.append("deck %s \n".format(this.deck))
      builder.append("hand %s \n".format(this.hand))
      builder.append("played %s \n".format(this.played))
      builder.append("discard %s \n".format(this.discard))
      builder.toString
    }
  }
  
  object Game {
    private val initDeck: Vector[Card] = Vector.fill(3)(Estate) ++ Vector.fill(7)(Copper)
    
    def create(playerNames: Vector[String]): Game = {
      val players = playerNames map Player.create 
      
      players foreach { player =>
        player.deck = util.Random.shuffle(initDeck)
        player.drawCards(5)
      }
      
      val game = new Game(
        players = players,
        activePlayerIndex = util.Random.nextInt(playerNames.length),
        trash = Vector.empty
      )
      game.activePlayer.activate()
      game
    }
  }
  
  object Player {
    def create(name: String): Player = 
      new Player(
        name = name, 
        stat = new PlayerStat(actions = 0, buys = 0, coins = 0),
        stage = PlayerStage.Inactive,
        deck = Vector.empty,
        hand = Vector.empty,
        played = Vector.empty,
        discard = Vector.empty
      )
  }
  
  class PlayerStat(var actions: Int, var buys: Int, var coins: Int) {
    override def toString: String = "actions:%d buys:%d coins:%d".format(this.actions, this.buys, this.coins)
  }
  
  object PlayerStage extends Enumeration {
    val Inactive, Action, Treasure, Buy, Cleanup = Value
  }
  
  class AskDialog(
    val player: Player,
    val message: String,
    val choices: Vector[String]
  )
  
  class AskException(val dialog: AskDialog) extends Exception
  
  trait Card {
    def name: String
    def cost: Int
    
    override def toString: String = name
  }
  
  trait Actionable {
    def play(game: Game, context: RestoreContext)
  }
  
  trait Treasurable {
    def coins: Int
  }
  
  object Copper extends Card with Treasurable {
    val name = "Copper"
    val cost = 0 
    val coins = 1
  }
  
  object Estate extends Card {
    val name = "Estate"
    val cost = 2
  }
  
  object Remodel extends Card with Actionable {
    val name = "Remodel"
    val cost = 4
    
    def play(game: Game, context: RestoreContext) {
      println("playing " + this)
      
      if (game.activePlayer.hand.isEmpty) {
        println("you have no card in hand to trash")
        return
      }
        
      val cardsToTrash = context.ask(game.activePlayer, "choose a card to trash", game.activePlayer.hand)
      if (cardsToTrash.isEmpty)
        throw context.pause(game.activePlayer, "you must choose one card to trash", game.activePlayer.hand)
      
      if (cardsToTrash.length > 1)  
        throw context.pause(game.activePlayer, "you can only choose one card to trash", game.activePlayer.hand)
      
      context.approve()
      val cardToTrash = cardsToTrash(0)
      
      game.moveCard(cardToTrash, game.activePlayer.hand, game.trash) { (from, to) =>
        game.activePlayer.hand = from
        game.trash = to
      }
      println("trashed " + cardToTrash)
      
      val supply = Vector(Remodel, ThroneRoom)
      val cardsToGain = context.ask(game.activePlayer, "gain a card up to %d coins".format(cardToTrash.cost + 2), supply)
      if (cardsToGain.isEmpty)
        throw context.pause(game.activePlayer, "you must gain a card up to %d coins".format(cardToTrash.cost + 2), supply)
      
      if (cardsToGain.length > 1)
        throw context.pause(game.activePlayer, "you can only gain one card up to %d coins".format(cardToTrash.cost + 2), supply)
        
      context.approve()
      val cardToGain = cardsToGain(0)
      
      game.activePlayer.discard :+= cardToGain
      println("gained " + cardToGain)
    }
  }
  
  object ThroneRoom extends Card with Actionable {
    val name = "Throne Room"
    val cost = 4
    
    def play(game: Game, context: RestoreContext) {
      println("playing " + this)
      
      val actionCards = game.activePlayer.hand collect { case card: Actionable => card }
      if (actionCards.isEmpty) {
        println("you have no action card in hand to play")
        return
      }
      
      val cardsToPlay = context.ask(game.activePlayer, "choose an action card to play twice", actionCards)
      if (cardsToPlay.isEmpty)
        throw context.pause(game.activePlayer, "you must choose one card to play twice", actionCards)
        
      if (cardsToPlay.length > 1)
        throw context.pause(game.activePlayer, "you must only choose one card to play twice", actionCards)
        
      context.approve()
      val cardToPlay = cardsToPlay(0)
      game.moveCard(cardToPlay, game.activePlayer.hand, game.activePlayer.played) { (from, to) => 
        game.activePlayer.hand = from
        game.activePlayer.played = to
      }
      
      (1 to 2).foreach { _ => cardToPlay.play(game, context) }
    }
  }
}