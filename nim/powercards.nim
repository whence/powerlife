import math
import sequtils
import algorithm
from strutils import nil

type
  Game = ref object
    players: seq[Player]
    active_player_index: int
    board: Board
    stage: Stage
    inout: InputOutput
    
  Stage = enum
    Action, Treasure, Buy, Cleanup

  Player = ref object
    name: string
    deck, hand, played, discarded: seq[Card]
    actions, buys, coins: int

  Board = ref object
    trash: seq[Card]
    piles: seq[Pile]

  CardFactory = proc (): Card {.locks: 0.}
  GamePlay = proc (game: Game) {.locks: 0.}

  Pile = ref object
    sample: Card
    factory: CardFactory
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
    FPlay: GamePlay
    
  TreasureCard = ref object of CardObj
    FCoins: int

  VictoryCard = ref object of CardObj
    FVps: int

  Hybrid = ref object of CardObj
    FVps: int

  Choice = tuple[name: string, selectable: bool]

  InputOutput = ref InputOutputObj
  InputOutputObj = object of RootObj
  RealInputOutput = ref object of InputOutputObj
  FakeInputOutput = ref object of InputOutputObj
    inBuf: seq[string]
    outBuf: seq[string]

proc active(game: Game): Player =
  game.players[game.active_player_index]

proc activate(p: Player) =
  p.actions = 1
  p.buys = 1
  p.coins = 0

proc deactivate(p: Player) =
  p.actions = 0
  p.buys = 0
  p.coins = 0

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

proc shuffle[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

proc cycle(x, cap: int): int =
  let y = x + 1
  return if y >= cap: 0 else: y

template any(seq1, pred: expr): expr =
  var result {.gensym.}: bool = false
  for it {.inject.} in items(seq1):
    result = pred
    if result:
      break
  result

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

proc newPile(factory: CardFactory, size: int): Pile =
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
    
proc moveOne(src, dst: var seq[Card], index: int): Card =
  result = src[index]
  system.delete(src, index)
  dst.add(result)

proc moveOne(pile: Pile, dst: var seq[Card]): Card =
  result = pile.pop
  dst.add(result)

proc moveMany(src, dst: var seq[Card], indexes: seq[int]): seq[Card] =
  result = indexes.mapit(Card, src[it])
  if result.len == src.len:
    src.setlen(0)
  else:
    var ix = indexes
    ix.sort(system.cmp[int], SortOrder.Descending)
    for i in ix:
      system.delete(src, i)
  dst.add(result)

proc moveAll(src, dst: var seq[Card]): seq[Card] {.discardable.} =
  result = src
  src.setlen(0)
  dst.add(result)

proc drawCardsNoRecycle(player: Player, n: int): seq[Card] =
  let first = player.deck.len - n
  let last = player.deck.len - 1
  result = player.deck[first..last]
  result.reverse
  sequtils.delete(player.deck, first, last)
  player.hand.add(result)

proc drawCardsFullDeck(player: Player): seq[Card] =
  result = player.deck
  result.reverse
  player.deck.setlen(0)
  player.hand.add(result)

proc drawCards(player: Player, n: int): seq[Card] {.discardable.} =
  if player.deck.len > n:
    return drawCardsNoRecycle(player, n)

  var cards = drawCardsFullDeck(player)

  while cards.len < n and player.discarded.len > 0:
    player.deck.add(player.discarded)
    player.discarded.setlen(0)
    player.deck.shuffle

    let remaining = n - cards.len
    if player.deck.len > remaining:
      cards.add(drawCardsNoRecycle(player, remaining))
      return cards

    cards.add(drawCardsFullDeck(player))

  return cards

method output(inout: InputOutput, message: string) = discard
method output(inout: FakeInputOutput, message: string) = inout.outBuf.add(message)
method output(inout: RealInputOutput, message: string) = echo(message)

method input(inout: InputOutput): string = nil
method input(inout: FakeInputOutput): string = inout.inBuf.pop
method input(inout: RealInputOutput): string = stdin.readline

proc outputChoices(inout: InputOutput, choices: seq[Choice]) =
  for i, c in choices:
    inout.output("[" & $i & "] " & c.name & " (" & $c.selectable & ")")

proc chooseOne(inout: InputOutput, message: string, choices: seq[Choice]): Response =
  if not choices.any(it.selectable):
    return Response(kind: Unselectable)

  while true:
    inout.output(message)
    inout.outputChoices(choices)
    let index = strutils.parseInt(inout.input())
    if choices[index].selectable:
      return Response(kind: One, index: index)

    inout.output(choices[index].name & " is not selectable")

proc chooseOptionalOne(inout: InputOutput, message: string, choices: seq[Choice]): Response =
  if not choices.any(it.selectable):
    return Response(kind: Unselectable)

  while true:
    inout.output(message)
    inout.outputChoices(choices)
    inout.output("or skip")
    let input = inout.input()
    if input == "skip":
      return Response(kind: Skip)
      
    let index = strutils.parseInt(input)
    if choices[index].selectable:
      return Response(kind: One, index: index)

    inout.output(choices[index].name & " is not selectable")

proc chooseUnlimited(inout: InputOutput, message: string, choices: seq[Choice]): Response =
  if not choices.any(it.selectable):
    return Response(kind: Unselectable)

  while true:
    inout.output(message)
    inout.outputChoices(choices)
    inout.output("or all, or skip")
    let input = inout.input()
    if input == "skip":
      return Response(kind: Skip)

    if input == "all":
      let indexes = toSeq(pairs(choices)).filterit(it.val.selectable).mapit(int, it.key)
      return Response(kind: Multi, indexes: indexes)
      
    var parts = strutils.split(input, ',')
    parts.mapit(strutils.strip(it))
    parts.keepitif(it.len > 0)
    let indexes = parts.map(strutils.parseint)
    
    if not indexes.any(not choices[it].selectable):
      return Response(kind: Multi, indexes: indexes)

    inout.output("some choices are not selectable")

proc playAction(game: Game) =
  if game.active.actions == 0:
    game.inout.output("no more actions, skip to treasure stage")
    game.stage = Treasure
  else:
    let r = game.inout.choose_optional_one("select an action card to play", game.active.hand.mapit(Choice, ($it, it.isActionable)))
    case r.kind:
    of One:
      let card = game.active.hand.move_one(game.active.played, r.index)
      game.active.actions -= 1
      game.inout.output("playing " & $card)
      card.play(game)
    of Skip, Unselectable:
      game.inout.output("skip to treasure stage")
      game.stage = Treasure
    else: discard

proc playTreasure(game: Game) =
  let r = game.inout.choose_unlimited("select treasure cards to play", game.active.hand.mapit(Choice, ($it, it.isTreasurable)))
  case r.kind:
  of Multi:
    let cards = game.active.hand.move_many(game.active.played, r.indexes)
    for c in cards:
      c.play(game)
  of Skip, Unselectable:
    game.inout.output("skip to buy stage")
    game.stage = Buy
  else: discard

proc playBuy(game: Game) =
  if game.active.buys == 0:
    game.inout.output("no more buys, skip to cleanup stage")
    game.stage = Cleanup
  else:
    let r = game.inout.choose_optional_one("select a pile to buy", game.board.piles.mapit(Choice, ($it.sample, not it.isEmpty and it.sample.cost(game) <= game.active.coins)))
    case r.kind:
    of One:
      let card = game.board.piles[r.index].move_one(game.active.discarded)
      game.inout.output("bought " & $card)
      game.active.actions -= card.cost(game)
      game.active.buys -= 1
    of Skip, Unselectable:
      game.inout.output("skip to cleanup stage")
      game.stage = Cleanup
    else: discard

proc playCleanup(game: Game) =
  game.active.hand.moveall(game.active.discarded)
  game.active.played.moveall(game.active.discarded)
  game.active.drawcards(5)
  game.active.deactivate
  game.active_player_index = cycle(game.active_player_index, game.players.len)
  game.active.activate

proc play(game: Game) =
  case game.stage:
  of Action: playAction(game)
  of Treasure: playTreasure(game)
  of Buy: playBuy(game)
  of Cleanup: playCleanup(game)

proc playRemodel(game: Game) =
  let rtrash = game.inout.choose_one("select a card to trash", game.active.hand.mapit(Choice, ($it, true)))
  case rtrash.kind:
  of One:
    let ctrash = game.active.hand.move_one(game.board.trash, rtrash.index)
    game.inout.output("trashed " & $ctrash)
    let rgain = game.inout.choose_one("select a pile to gain", game.board.piles.mapit(Choice, ($it.sample, not it.isEmpty and it.sample.cost(game) <= ctrash.cost(game) + 2)))
    case rgain.kind:
    of One:
      let cgain = game.board.piles[rgain.index].move_one(game.active.discarded)
      game.inout.output("gained " & $cgain)
    of Unselectable:
      game.inout.output("no pile available to gain")
    else: discard
  of Unselectable:
    game.inout.output("no card in hand to trash")
  else: discard

proc newRemodel(): Card = ActionCard(FName: "Remodel", FBaseCost: 4, FPlay: playRemodel)

proc playSmithy(game: Game) =
  game.active.drawCards(3)
  
proc newSmithy(): Card = ActionCard(FName: "Smithy", FBaseCost: 4, FPlay: playSmithy)

proc playThroneRoom(game: Game) =
  let r = game.inout.choose_one("select an action card to play twice", game.active.hand.mapit(Choice, ($it, it.isActionable)))
  case r.kind:
  of One:
    let card = game.active.hand.move_one(game.active.played, r.index)
    game.inout.output("playing " & $card & " first time")
    card.play(game)
    game.inout.output("playing " & $card & " second time")
    card.play(game)
  of Unselectable:
    game.inout.output("no action card available to play")
  else: discard

proc newThroneRoom(): Card = ActionCard(FName: "Throne Room", FBaseCost: 4, FPlay: playThroneRoom)

proc newGame(names: openArray[string], inout: InputOutput): Game =
  new(result)
  result.inout = inout
  result.players = names.map(newPlayer)
  result.active_player_index = random(result.players.len)
  result.stage = Action
  result.board = Board(trash: @[], piles: @[newPile(newCopper, 60), newPile(newEstate, 12), newPile(newRemodel, 10), newPile(newSmithy, 10), newPile(newThroneRoom, 10)])
  result.active.activate

when isMainModule:
  randomize()

  block: # pile push and pop
    let pile = newPile(newCopper, 8)
    assert pile.isempty == false
    for i in 1..5:
      discard pile.pop
      assert pile.size == 8 - i
    for i in 1..3:
      pile.push(newCopper())
      assert pile.size == 3 + i
    for i in 1..6:
      discard pile.pop
      assert pile.size == 6 - i
    assert pile.isempty
    for i in 1..2:
      pile.push(newCopper())
      assert pile.size == i
    assert pile.isempty == false

  block: # pile should pop different cards
    let pile = newPile(newCopper, 2)
    let card1 = pile.pop
    let card2 = pile.pop
    assert card1 != pile.sample
    assert card1 != card2

  block: # pile should pop the same card as pushed
    let pile = newPile(newCopper, 2)
    let card1 = newCopper()
    pile.push(card1)
    let card2 = pile.pop
    assert card1 == card2

  block: # game init
    let game = newGame(["wes", "bec"], FakeInputOutput())
    assert game.players.len == 2
    assert game.players.mapit(string, it.name) == @["wes", "bec"]
