require_relative 'player'
require_relative 'board'

class Game
  attr_accessor :actions, :buys, :coins
  attr_reader :active, :board, :players

  def initialize(player_names, kingdom_cards)
    fail 'only 2 to 4 players allowed' unless player_names.length.between? 2, 4

    @players = player_names.map { |name| Player.new(name) }
    @active_index = rand(player_names.length)
    @active = @players[@active_index]

    @board = Board.new kingdom_cards, @players.length

    @actions, @buys, @coins = 1, 1, 0
    @stage = :action
  end

  def continue
    send("#{@stage}_stage")
  end

  private

  def action_stage
    if @actions > 0
      @active.choose(
        message: 'select an action card to play',
        items: @active.hand,
        each: ->(c) { c.is_a? ActionCard },
        limit: :one,
        commands: {
          unable: lambda do
            puts 'no action card, skipping to treasure stage'
            @actions = 0
            @stage = :treasure
          end,
          skip: lambda do
            puts 'skipping to treasure stage'
            @actions = 0
            @stage = :treasure
          end,
          play: lambda do |index|
            action_card = @active.move_one index, :hand, :to_played
            @actions -= 1
            puts "playing #{action_card}"
            action_card.play(self)
          end
        }
      )
    else
      puts 'no more actions, skipping to treasure stage'
      @stage = :treasure
    end
  end

  def treasure_stage
    @active.choose(
      message: 'select treasure cards to play',
      items: @active.hand,
      each: ->(c) { c.is_a? TreasureCard },
      limit: :one_to_many,
      commands: {
        unable: lambda do
          puts 'no treasure card, skipping to buy stage'
          @stage = :buy
        end,
        skip: lambda do
          puts 'skipping to buy stage'
          @stage = :buy
        end,
        play: lambda do |indexes|
          # TODO: do not move/play in bulk if not all are basic treasures
          treasure_cards = @active.move_many indexes, :hand, :to_played
          puts "playing #{treasure_cards.join(', ')}"
          treasure_cards.each { |c| c.play(self) }
        end
      }
    )
  end

  def buy_stage
    if @buys > 0
      @active.choose(
        message: 'select a pile to buy',
        items: @board.piles,
        each: ->(pile) { !pile.empty? && pile.sample.costs <= @coins },
        limit: :one,
        commands: {
          unable: lambda do
            puts 'no pile available to buy, skipping to cleanup stage'
            @buys = 0
            @stage = :cleanup
          end,
          skip: lambda do
            puts 'skipping to cleanup stage'
            @buys = 0
            @stage = :cleanup
          end,
          buy: lambda do |index|
            bought_card = @active.gain_from_pile(@board.piles[index])
            @buys -= 1
            @coins -= bought_card.costs
            puts "bought #{bought_card}"
          end
        }
      )
    else
      puts 'no more buys, skipping to cleanup stage'
      @stage = :cleanup
    end
  end

  def cleanup_stage
    @active.cleanup
    @active_index += 1
    @active_index = 0 if @active_index >= @players.length
    @active = @players[@active_index]
    @actions, @buys, @coins = 1, 1, 0
    @stage = :action
  end
end
