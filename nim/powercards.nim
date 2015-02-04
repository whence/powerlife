import math
import sequtils except delete

type
  Game = ref object
    players: seq[Player]
    active_player_index: int
    board: Board
    stage: Stage
    
  Stage = enum
    Action, Treasure, Buy, Cleanup

  Player = ref object
    name: string
    deck, hand, played, discarded: seq[Card]
    actions, buys, coins: int

  Board = ref object
    trash: seq[Card]
    piles: seq[Pile]

  Pile = ref object
    sample: Card
    factory: proc (): Card
    size: int
    buffer: seq[Card]

  ResponseKind = enum
    Unselectable, Skip, One, Multi

  Response = ref object
    case kind: ResponseKind
    of Unselectable, Skip: reason: string
    of One: index: int
    of Multi: indexes: seq[int]

  Card = ref CardObj
  CardObj = object of RootObj
    FName: string
    FBaseCost: int

  ActionCard = ref object of CardObj
    FPlay: proc (game: Game) {.locks: 0.}
    
  TreasureCard = ref object of CardObj
    FCoins: int

  VictoryCard = ref object of CardObj
    FVps: int

  Hybrid = ref object of CardObj
    FVps: int

  Choice = tuple[name: string, selectable: bool]

proc active(game: Game): Player =
  game.players[game.active_player_index]

proc `$`(card: Card): string = card.FName

method play(card: Card, game: Game) = discard
method play(card: ActionCard, game: Game) = card.FPlay(game)
method play(card: TreasureCard, game: Game) = game.active.coins += card.FCoins
method play(card: Hybrid, game: Game) = echo("playing " & $card)

method cost(card: Card, game: Game): int = card.FBaseCost
method cost(card: Hybrid, game: Game): int = 99

method victoryPoint(card: Card, game: Game): int = 0
method victoryPoint(card: VictoryCard, game: Game): int = card.FVps
method victoryPoint(card: Hybrid, game: Game): int = card.FVps

method isActionable(card: Card): bool = false
method isActionable(card: ActionCard): bool = true

method isTreasurable(card: Card): bool = false
method isTreasurable(card: TreasureCard): bool = true
method isTreasurable(card: Hybrid): bool = true

method isVictoriable(card: Card): bool = false
method isVictoriable(card: VictoryCard): bool = true
method isVictoriable(card: Hybrid): bool = true

proc newCopper(): Card = TreasureCard(FName: "Copper", FBaseCost: 0, FCoins: 1)
proc newEstate(): Card = VictoryCard(FName: "Estate", FBaseCost: 2, FVps: 1)

proc playRemodel(game: Game) = echo("playing remodel")
proc newRemodel: Card = ActionCard(FName: "Remodel", FBaseCost: 4, FPlay: playRemodel)

proc playSmithy(game: Game)= echo("playing smithy")
proc newSmithy: Card = ActionCard(FName: "Smithy", FBaseCost: 4, FPlay: playSmithy)

proc shuffle[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

proc newPlayer(name: string): Player =
  new(result)
  result.name = name
  var deck = newSeq[Card](10)
  for i in 0..2:
    deck[i] = newEstate()
  for i in 3..9:
    deck[i] = newCopper()
  deck.shuffle
  result.deck = deck[0..4]
  result.hand = deck[5..9]
  result.played = @[]
  result.discarded = @[]
  result.actions = 0
  result.buys = 0
  result.coins = 0

proc newPile(factory: proc (): Card, size: int): Pile =
  new(result)
  result.factory = factory
  result.size = size
  result.sample = factory()
  result.buffer = @[]

proc isEmpty(pile: Pile): bool = pile.size <= 0

proc push(pile: Pile, card: Card) =
  pile.buffer.add(card)
  pile.size += 1

proc pop(pile: Pile): Card =
  if pile.buffer.len > 0:
    result = pile.buffer.pop
  else:
    result = pile.factory()
  pile.size -= 1
    
proc newGame(names: openArray[string]): Game =
  new(result)
  result.players = names.map(newPlayer)
  result.active_player_index = random(result.players.len)
  result.stage = Action
  result.board = Board(trash: @[], piles: @[newPile(newCopper, 60), newPile(newEstate, 12)])
  result.active.actions = 1
  result.active.buys = 1

proc moveOne(src, dst: seq[Card], index: int): Card =
  src[index]

randomize()

let game = newGame(["wes", "bec"])
#echo(game.repr)

#echo($game.active.hand)
let cards = game.active.hand.filterit(it.isTreasurable)
#echo($cards)

let pile = newPile(newCopper, 10)

let responses = @[Response(kind: ONE, index: 1), Response(kind: SKIP, reason: "no card to select")]

when false:
  for r in responses:
    case r.kind:
      of ONE: echo("one")
      of SKIP: echo("skip")
      else: echo("else")

let cards2 = @[newCopper(), newEstate(), Hybrid(FName: "Hybrid")]
echo($cards2)
let choices = cards2.mapit(Choice, ($it, it.isActionable))
echo($choices)