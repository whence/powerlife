import math
import sequtils

type
  Game = ref object
    players: seq[Player]
    active_player_index: int
    board: Board
    stage: Stage
    
  Stage = enum
    ACTION, TREASURE, BUY, CLEANUP  

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
    UNSELECTABLE, SKIP, ONE, MULTI

  Response = object
    case kind: ResponseKind
    of UNSELECTABLE, SKIP: reason: string
    of ONE: index: int
    of MULTI: indexes: seq[int]

  Card = ref CardObj
  CardObj = object of RootObj

  ActionCard = ref ActionCardObj
  ActionCardObj = object of CardObj

  TreasureCard = ref TreasureCardObj
  TreasureCardObj = object of CardObj

  VictoryCard = ref VictoryCardObj
  VictoryCardObj = object of CardObj

  Copper = ref object of TreasureCardObj
  Estate = ref object of VictoryCardObj

proc shuffle[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

method name(card: Card): string = "Card"
method name(card: Copper): string = "Copper"
method name(card: Estate): string = "Estate"
proc `$`(card: Card): string = card.name

proc print(cards: openArray[Card]) =
  for i, c in cards:
    echo($i & c.name)

proc newPlayer(name: string): Player =
  new(result)
  result.name = name
  var deck = newSeq[Card](10)
  for i in 0..2:
    deck[i] = Estate()
  for i in 3..9:
    deck[i] = Copper()
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
    
proc active(game: Game): Player =
  game.players[game.active_player_index]

proc newGame(names: openArray[string]): Game =
  new(result)
  result.players = names.map(newPlayer)
  result.active_player_index = random(result.players.len)
  result.stage = ACTION
  result.board = Board(trash: @[], piles: @[newPile(proc(): Card = Copper(), 60), newPile(proc(): Card = Estate(), 12)])
  result.active.actions = 1
  result.active.buys = 1

method cost(card: Card): int = 0
method cost(card: Copper): int = 0
method cost(card: Estate): int = 2

method play(card: ActionCard) = discard

method play(card: TreasureCard, game: Game) = discard
method play(card: Copper, game: Game) =
  game.active.coins += 1

method victoryPoint(card: VictoryCard): int = 0
method victoryPoint(card: Estate): int = 1

randomize()

let game = newGame(["wes", "bec"])
#echo(game.repr)

#echo($game.active.hand)
let cards = game.active.hand.filter(proc(c: Card): bool = c of TreasureCard)
#echo($cards)

let pile = newPile(proc(): Card = Copper(), 10)

let responses = @[Response(kind: ONE, index: 1), Response(kind: SKIP, reason: "no card to select")]

for r in responses:
  case r.kind:
    of ONE: echo("one")
    of SKIP: echo("skip")
    else: echo("else")
