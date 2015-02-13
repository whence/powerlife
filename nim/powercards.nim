import sequtils
import math
import algorithm
from strutils import nil
import utils

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

  CardSupplier = proc (): Card {.locks: 0.}
  CardsConsumer = proc (cards: var seq[Card]) {.locks: 0.}
  GameConsumer = proc (game: Game) {.locks: 0.}

  Pile = ref object
    sample: Card
    supplier: CardSupplier
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
    FPlay: GameConsumer
    
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
    shuffleBuf: seq[CardsConsumer]

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
method play(card: Hybrid, game: Game) = echo("playing ", card)

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
proc newSilver(): Card = TreasureCard(FName: "Silver", FBaseCost: 3, FCoins: 2)
proc newGold(): Card = TreasureCard(FName: "Gold", FBaseCost: 6, FCoins: 3)

proc newEstate(): Card = VictoryCard(FName: "Estate", FBaseCost: 2, FVps: 1)
proc newDuchy(): Card = VictoryCard(FName: "Duchy", FBaseCost: 5, FVps: 3)
proc newProvince(): Card = VictoryCard(FName: "Province", FBaseCost: 8, FVps: 6)

method output(inout: InputOutput, message: string) = discard
method output(inout: FakeInputOutput, message: string) = inout.outbuf.add(message)
method output(inout: RealInputOutput, message: string) = echo(message)

method input(inout: InputOutput): string = nil
method input(inout: FakeInputOutput): string = inout.inbuf.pop
method input(inout: RealInputOutput): string = stdin.readline

method shuffle(inout: InputOutput, x: var seq[Card]) = discard
method shuffle(inout: FakeInputOutput, x: var seq[Card]) =
  let consumer = inout.shufflebuf.pop
  consumer(x)

method shuffle(inout: RealInputOutput, x: var seq[Card]) = shuffle(x)

proc newPlayer(name: string): Player =
  new(result)
  result.name = name
  result.deck = newSeq[Card](10)
  for i in 0..2:
    result.deck[i] = newEstate()
  for i in 3..9:
    result.deck[i] = newCopper()
  result.hand = @[]
  result.played = @[]
  result.discarded = @[]
  result.actions = 0
  result.buys = 0
  result.coins = 0

proc newPile(supplier: CardSupplier, size: int): Pile =
  new(result)
  result.supplier = supplier
  result.size = size
  result.sample = supplier()
  result.buffer = @[]

proc isEmpty(pile: Pile): bool = pile.size <= 0

proc push(pile: Pile, card: Card) =
  pile.buffer.add(card)
  pile.size += 1

proc pop(pile: Pile): Card =
  if pile.buffer.len > 0:
    result = pile.buffer.pop
  else:
    result = pile.supplier()
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
    src.clear
  else:
    var ix = indexes
    ix.sort(system.cmp[int], SortOrder.Descending)
    for i in ix:
      system.delete(src, i)
  dst.add(result)

proc moveAll(src, dst: var seq[Card]): seq[Card] {.discardable.} =
  result = src
  src.clear
  dst.add(result)

proc drawCardsNoRecycle(player: Player, n: int): seq[Card] =
  let
    first = player.deck.len - n
    last = player.deck.len - 1
  result = player.deck[first..last]
  result.reverse
  sequtils.delete(player.deck, first, last)
  player.hand.add(result)

proc drawCardsFullDeck(player: Player): seq[Card] =
  result = player.deck
  result.reverse
  player.deck.clear
  player.hand.add(result)

proc drawCards(player: Player, n: int, inout: InputOutput): seq[Card] {.discardable.} =
  if player.deck.len > n:
    return drawCardsNoRecycle(player, n)

  var cards = drawCardsFullDeck(player)

  while cards.len < n and player.discarded.len > 0:
    player.deck.add(player.discarded)
    player.discarded.clear
    inout.shuffle(player.deck)

    let remaining = n - cards.len
    if player.deck.len > remaining:
      cards.add(drawCardsNoRecycle(player, remaining))
      return cards

    cards.add(drawCardsFullDeck(player))

  return cards

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

proc prepare(player: Player, inout: InputOutput) =
  inout.shuffle(player.deck)
  player.drawcards(5, inout)

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
      game.active.coins -= card.cost(game)
      game.active.buys -= 1
    of Skip, Unselectable:
      game.inout.output("skip to cleanup stage")
      game.stage = Cleanup
    else: discard

proc playCleanup(game: Game) =
  game.active.hand.moveall(game.active.discarded)
  game.active.played.moveall(game.active.discarded)
  game.active.drawcards(5, game.inout)
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
  game.active.drawCards(3, game.inout)
  
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
  for p in result.players:
    p.prepare(inout)
  result.active.activate

when isMainModule:
  randomize()

  proc noShuffle(cards: var seq[Card]) = discard
  proc doShuffle(cards: var seq[Card]) = shuffle(cards)

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
    let
      pile = newPile(newCopper, 2)
      card1 = pile.pop
      card2 = pile.pop
    assert card1 != pile.sample
    assert card1 != card2

  block: # pile should pop the same card as pushed
    let
      pile = newPile(newCopper, 2)
      card1 = newCopper()
    pile.push(card1)
    let card2 = pile.pop
    assert card1 == card2

  block: # draw cards
    let cards = [newCopper(), newSilver(), newGold(), newEstate(), newDuchy(), newProvince()]

    block: # simple
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[])
      player.deck.replace([cards[0], cards[1], cards[2]])
      player.hand.replace([cards[3]])

      let result = player.drawcards(2, inout)

      assert result == @[cards[2], cards[1]]
      assert player.deck == @[cards[0]]
      assert player.hand == @[cards[3], cards[2], cards[1]]

    block: # when deck is empty
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[noshuffle])
      player.deck.clear
      player.hand.replace([cards[0], cards[1]])
      player.discarded.replace([cards[2]])

      let result = player.drawcards(1, inout)

      assert result == @[cards[2]]
      assert player.deck.len == 0
      assert player.hand == @[cards[0], cards[1], cards[2]]
      assert player.discarded.len == 0

    block: # full deck
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[])
      player.deck.replace([cards[0], cards[1], cards[2], cards[3]])
      player.hand.clear
      player.discarded.replace([cards[4], cards[5]])

      let result = player.drawcards(4, inout)

      assert result == @[cards[3], cards[2], cards[1], cards[0]]
      assert player.deck.len == 0
      assert player.hand == @[cards[3], cards[2], cards[1], cards[0]]
      assert player.discarded == @[cards[4], cards[5]]

    block: # recycle
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[noshuffle])
      player.deck.replace([cards[0], cards[1], cards[2]])
      player.hand.replace([cards[3]])
      player.discarded.replace([cards[4], cards[5]])

      let result = player.drawcards(4, inout)

      assert result == @[cards[2], cards[1], cards[0], cards[5]]
      assert player.deck == @[cards[4]]
      assert player.hand == @[cards[3], cards[2], cards[1], cards[0], cards[5]]
      assert player.discarded.len == 0
        
    block: # draw all and recycle
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[noshuffle])
      player.deck.replace([cards[0], cards[1], cards[2]])
      player.hand.replace([cards[3]])
      player.discarded.replace([cards[4], cards[5]])

      let result = player.drawcards(5, inout)

      assert result == @[cards[2], cards[1], cards[0], cards[5], cards[4]]
      assert player.hand == @[cards[3], cards[2], cards[1], cards[0], cards[5], cards[4]]
      assert player.deck.len == 0
      assert player.discarded.len == 0
        
    block: # draw stops when no more
      let
        player = newPlayer("wes")
        inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[noshuffle])
      player.deck.replace([cards[0], cards[1]])
      player.hand.replace([cards[2], cards[3]])
      player.discarded.replace([cards[4], cards[5]])

      let result = player.drawcards(5, inout)
      
      assert result == @[cards[1], cards[0], cards[5], cards[4]]
      assert player.hand == @[cards[2], cards[3], cards[1], cards[0], cards[5], cards[4]]
      assert player.deck.len == 0
      assert player.discarded.len == 0
        
  block: # game init
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    assert game.players.len == 2
    assert game.players.mapit(string, it.name) == @["wes", "bec"]

    for p in game.players:
      assert p.deck.len == 5
      assert p.hand.len == 5
      assert p.played.len == 0
      assert p.discarded.len == 0

      var fulldeck: seq[Card] = @[]
      fulldeck.add(p.deck)
      fulldeck.add(p.hand)
      assert fulldeck.filterit($it == "Copper").len == 7
      assert fulldeck.filterit($it == "Estate").len == 3

      assert p.hand.any(it.isActionable) == false

      if p == game.active:
        assert p.actions == 1
        assert p.buys == 1
      else:
        assert p.actions == 0
        assert p.buys == 0
      assert p.coins == 0

      assert game.board.trash.len == 0
      assert game.stage == Action

  block: # first play should skip to treasure
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    game.play
    assert game.stage == Treasure
    assert inout.outbuf == @["skip to treasure stage"]

  block: # skip to treasure if no action points
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    game.active.actions = 0
    game.play
    assert game.stage == Treasure

  block: # playing action cards
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
      actionCard: Card = ActionCard(FName: "Dummy", FBaseCost: 4, FPlay: proc (game: Game) = game.inout.output("i am dummy"))
      hand = [newCopper(), newEstate(), actionCard, newEstate(), newSilver()]
    game.active.hand.replace(hand)
    inout.inbuf.add("2")

    assert game.active.actions == 1
    assert game.active.played.len == 0

    game.play

    assert game.active.actions == 0
    assert game.active.played == @[actionCard]
    assert game.active.hand == @[hand[0], hand[1], hand[3], hand[4]]
    assert game.stage == Action
    assert inout.outbuf.hasinorder(["i am dummy"])

  block: # playing treasure cards
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
      hand = [newCopper(), newEstate(), newGold(), newEstate(), newSilver()]      
    game.stage = Treasure
    game.active.hand.replace(hand)
    inout.inbuf.add("0, 2, 4")

    assert game.active.coins == 0
    assert game.active.played.len == 0

    game.play

    assert game.active.coins == 6
    assert game.active.played == @[hand[0], hand[2], hand[4]]
    assert game.active.hand == @[hand[1], hand[3]]
    assert game.stage == Treasure

  block: # skip to buy if no treasure cards
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    game.stage = Treasure
    game.active.hand.replace([newEstate(), newDuchy(), newProvince()])
    game.play
    assert game.stage == Buy

  block: # buy
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    game.stage = Buy
    game.board.piles.replace([newPile(newCopper, 10), newPile(newEstate, 8), newPile(newProvince, 8), newPile(newThroneRoom, 10), newPile(newRemodel, 10)])
    game.active.hand.clear
    game.active.coins = 5
    inout.inbuf.add("4")

    assert game.active.buys == 1
    assert game.active.played.len == 0

    game.play

    assert game.active.discarded.mapit(string, $it) == @["Remodel"]
    assert game.active.coins == 1
    assert game.active.buys == 0
    assert game.board.piles.filterit($it.sample == "Remodel")[0].size == 9
    assert game.stage == Buy
    assert inout.outbuf.hasinorder(["bought Remodel"])

  block: # skip to cleanup if no buy points
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
    game.stage = Buy
    game.active.buys = 0
    game.play
    assert game.stage == Cleanup

  block: # play remodel
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
      hand = [newEstate(), newRemodel(), newCopper(), newEstate(), newCopper()]
    game.board.piles.replace([newPile(newCopper, 10), newPile(newEstate, 8), newPile(newThroneRoom, 4)])
    game.active.hand.replace(hand)
    inout.inbuf.add(["2", "2", "1"])

    game.play

    assert game.active.played == @[hand[1]]
    assert game.active.hand == @[hand[0], hand[2], hand[4]]
    assert game.active.discarded.mapit(string, $it) == @["Throne Room"]
    assert game.board.trash == @[hand[3]]
    assert game.board.piles.filterit($it.sample == "Throne Room")[0].size == 3
    assert game.active.actions == 0
    assert inout.outbuf.hasinorder(["trashed Estate", "gained Throne Room"])

  block: # throneroom throneroom smithy
    let
      inout = FakeInputOutput(inbuf: @[], outbuf: @[], shufflebuf: @[doshuffle, doshuffle])
      game = newGame(["wes", "bec"], inout)
      treasures = [newCopper(), newSilver(), newGold()]
    game.board.piles.clear
    game.active.hand.replace([newSmithy(), newThroneRoom(), newSmithy(), newThroneRoom(), newCopper()])
    game.active.deck.clear
    for i in 1..4:
      game.active.deck.add(treasures)
    inout.inbuf.add(["all", "0", "1", "2", "1"])

    game.play # play actions
    game.play # skip to treasure
    game.play # play treasures

    assert inout.inbuf.len == 0
    assert game.active.hand.len == 0
    assert game.active.actions == 0
    assert game.active.coins == 25
    assert game.stage == Treasure
    assert inout.outbuf.hasinorder(["playing Throne Room first time", "playing Smithy first time", "playing Smithy second time", "playing Throne Room second time", "playing Smithy first time", "playing Smithy second time"])

  echo("All tests passed")
