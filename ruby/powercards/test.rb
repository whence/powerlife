require_relative 'game'
require_relative 'player'
require_relative 'board'
require_relative 'cardlib'

describe Game do
  it 'init' do
    game = Game.new %w(wes bec), []
    [game.actions, game.buys, game.coins].should eq([1, 1, 0])
    game.active.hand.any? { |c| c.is_a? ActionCard }.should eq(false)
  end
end

describe Player do
  it 'init' do
    player = Player.new 'wes'
    [:deck, :hand, :played, :discard].map do |m|
      player.send(m).length
    end.should eq([5, 5, 0, 0])

    fulldeck = player.deck + player.hand
    fulldeck.count { |c| c.instance_of? Copper }.should eq(7)
    fulldeck.count { |c| c.instance_of? Estate }.should eq(3)
  end

  it 'play one' do
    player = Player.new 'wes'
    player.hand.replace [1, 2, 3]
    player.played.replace [6, 7, 8]
    player.move_one(1, :hand, :to_played).should eq(2)
    player.hand.should eq([1, 3])
    player.played.should eq([6, 7, 8, 2])
  end

  it 'play many' do
    player = Player.new 'wes'
    player.hand.replace [1, 2, 3, 4, 5]
    player.played.replace [6, 7, 8, 9, 10]
    player.move_many([1, 2, 4], :hand, :to_played).should eq([2, 3, 5])
    player.hand.should eq([1, 4])
    player.played.should eq([6, 7, 8, 9, 10, 2, 3, 5])
  end

  it 'play empty' do
    player = Player.new 'wes'
    player.hand.replace [1, 2, 3, 4, 5]
    player.played.replace [6, 7, 8, 9, 10]
    player.move_many([], :hand, :to_played).should eq([])
    player.hand.should eq([1, 2, 3, 4, 5])
    player.played.should eq([6, 7, 8, 9, 10])
  end

  it 'play all' do
    player = Player.new 'wes'
    player.hand.replace [1, 2, 3, 4, 5]
    player.played.replace [6, 7, 8, 9, 10]
    player.move_many([0, 1, 2, 3, 4], :hand, :to_played).should eq([1, 2, 3, 4, 5])
    player.hand.should eq([])
    player.played.should eq([6, 7, 8, 9, 10, 1, 2, 3, 4, 5])
  end

  it 'trash one' do
    player = Player.new 'wes'
    player.hand.replace [1, 2, 3]
    board = Board.new [], 2
    board.trash.replace [6, 7, 8]
    player.move_one(1, :hand, [:to_trash, board]).should eq(2)
    player.hand.should eq([1, 3])
    board.trash.should eq([6, 7, 8, 2])
  end

  it 'draw less' do
    player = Player.new 'wes'
    player.deck.replace [1, 2, 3, 4, 5]
    player.hand.replace [6, 7]
    player.draw_cards(3).should eq([5, 4, 3])
    player.deck.should eq([1, 2])
    player.hand.should eq([6, 7, 5, 4, 3])
  end

  it 'draw one' do
    player = Player.new 'wes'
    player.deck.replace [1, 2, 3, 4]
    player.hand.replace [6, 7, 8]
    player.draw_cards(1).should eq([4])
    player.deck.should eq([1, 2, 3])
    player.hand.should eq([6, 7, 8, 4])
  end

  it 'draw all' do
    player = Player.new 'wes'
    player.deck.replace [1, 2, 3, 4, 5]
    player.hand.replace [6, 7]
    player.draw_cards(5).should eq([5, 4, 3, 2, 1])
    player.deck.should eq([])
    player.hand.should eq([6, 7, 5, 4, 3, 2, 1])
  end

  it 'draw more' do
    player = Player.new 'wes'
    player.deck.replace [1, 2, 3]
    player.hand.replace [6, 7, 8, 9]
    player.discard.replace [10, 11]
    cards_drawn = player.draw_cards(5)
    cards_drawn.first(3).should eq([3, 2, 1])
    cards_drawn.last(2).sort.should eq([10, 11])
    player.deck.should eq([])
    player.hand.first(7).should eq([6, 7, 8, 9, 3, 2, 1])
    player.hand.last(2).sort.should eq([10, 11])
    player.hand.length.should eq(9)
    player.discard.should eq([])
  end

  it 'draw too much' do
    player = Player.new 'wes'
    player.deck.replace [1, 2]
    player.hand.replace [6, 7, 8]
    player.discard.replace []
    player.draw_cards(5).should eq([2, 1])
    player.deck.should eq([])
    player.hand.should eq([6, 7, 8, 2, 1])
  end

  it 'draw too much even after recycle' do
    player = Player.new 'wes'
    player.deck.replace [1, 2]
    player.hand.replace [6, 7, 8]
    player.discard.replace [10, 11, 12]
    cards_drawn = player.draw_cards(10)
    cards_drawn.first(2).should eq([2, 1])
    cards_drawn.last(3).sort.should eq([10, 11, 12])
    player.deck.should eq([])
    player.hand.first(5).should eq([6, 7, 8, 2, 1])
    player.hand.last(3).sort.should eq([10, 11, 12])
    player.hand.length.should eq(8)
    player.discard.should eq([])
  end

  it 'gain 1 from pile' do
    player = Player.new 'wes'
    pile = Pile.new Copper, 10
    player.discard.replace [1, 2, 3]
    player.gain_from_pile pile
    player.discard.last.is_a?(Copper).should eq(true)
    player.discard.first(3).should eq([1, 2, 3])
    pile.remaining.should eq(9)
  end

  it 'cleanup' do
    player = Player.new 'wes'
    player.deck.replace [-6, -5, -4, -3, -2, -1]
    player.hand.replace [1, 2, 3]
    player.played.replace [4, 5]
    player.discard.replace [6, 7, 8, 9]
    player.cleanup
    player.deck.should eq([-6])
    player.hand.should eq([-1, -2, -3, -4, -5])
    player.played.should eq([])
    player.discard.should eq([6, 7, 8, 9, 1, 2, 3, 4, 5])
  end
end

describe 'cards' do
  it 'matches type' do
    [Copper, Silver, Gold].each do |cf|
      cf.new.is_a?(Card).should eq(true)
      cf.new.is_a?(TreasureCard).should eq(true)
    end

    [Estate, Duchy, Province, Gardens].each do |cf|
      cf.new.is_a?(Card).should eq(true)
      cf.new.is_a?(VictoryCard).should eq(true)
      cf.new.is_a?(ScoringCard).should eq(true)
    end

    [Curse, Gardens].each do |cf|
      cf.new.is_a?(Card).should eq(true)
      cf.new.is_a?(ScoringCard).should eq(true)
    end

    [Adventurer, Bureaucrat, Cellar, Chancellor, Chapel,
     CouncilRoom, Feast, Festival, Laboratory, Library,
     Market, Militia, Mine, Moat, Moneylender,
     Remodel, Smithy, Spy, Thief, ThroneRoom,
     Village, Witch, Woodcutter, Workshop].each do |cf|
      cf.new.is_a?(Card).should eq(true)
      cf.new.is_a?(ActionCard).should eq(true)
    end
  end
end

describe Board do
  it 'init' do
    board = Board.new [], 2
    board.trash.length.should eq(0)
  end
end
