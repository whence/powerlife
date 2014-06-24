class Card
  attr_reader :costs

  def to_s
    self.class.name
  end
end

module ActionCard
end

module TreasureCard
  def play(game)
    game.coins += coins
  end
end

module VictoryCard
end

module ScoringCard
  def score(_)
    vp
  end
end

class Copper < Card
  include TreasureCard

  def initialize
    @costs = 0
  end

  def coins
    1
  end
end

class Silver < Card
  include TreasureCard

  def initialize
    @costs = 3
  end

  def coins
    2
  end
end

class Gold < Card
  include TreasureCard

  def initialize
    @costs = 6
  end

  def coins
    3
  end
end

class Estate < Card
  include VictoryCard, ScoringCard

  def initialize
    @costs = 2
  end

  def vps
    1
  end
end

class Duchy < Card
  include VictoryCard, ScoringCard

  def initialize
    @costs = 5
  end

  def vps
    3
  end
end

class Province < Card
  include VictoryCard, ScoringCard

  def initialize
    @costs = 8
  end

  def vps
    6
  end
end

class Curse < Card
  include ScoringCard

  def initialize
    @costs = 0
  end

  def vps
    -1
  end
end

class Adventurer < Card
  include ActionCard

  def initialize
    @costs = 6
  end

  def play(game)
    cards_revealed = []
    loop do
      cards_drawn = game.active.draw_cards 1, target: nil
      cards_revealed.concat cards_drawn
      if cards_drawn.empty? || (cards_revealed.count { |x| x.is_a? TreasureCard } >= 2)
        treasures, non_treasures = cards_revealed.partition { |x| x.is_a? TreasureCard }
        unless treasures.empty?
          game.active.to_hand(treasures)
          puts "#{game.active.name} drawn #{treasures.join(', ')}"
        end
        unless non_treasures.empty?
          game.active.to_discard(non_treasures)
          puts "#{game.active.name} discarded #{non_treasures.join(', ')}"
        end
        break
      end
    end
  end
end

class Bureaucrat < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    silver_pile = game.board.piles.find { |x| x.sample.is_a? Silver }
    unless silver_pile.empty?
      gained_silver = game.active.gain_from_pile silver_pile, target: :deck
      puts "gained a #{gained_silver} to deck"
    end

    game.active.opponents(game).each do |player|
      next if player.defend?

      puts "#{player.name} revealed hand of #{player.hand.join(', ')}"

      player.choose(
        message: 'select a victory card to put back to deck',
        items: player.hand,
        each: ->(c) { c.is_a? VictoryCard },
        limit: :one,
        commands: {
          unable: lambda do
            puts 'no victory card in hand'
          end,
          autoplay: lambda do |index|
            victory_card = player.move_one index, :hand, :to_deck
            puts "#{player.name} put #{victory_card} back to top of deck"
          end,
          putback: lambda do |index|
            victory_card = player.move_one index, :hand, :to_deck
            puts "#{player.name} put #{victory_card} back to top of deck"
          end
        }
      )
    end
  end
end

class Cellar < Card
  include ActionCard

  def initialize
    @costs = 2
  end

  def play(game)
    game.actions += 1

    game.active.choose(
      message: 'select any number of cards to discard, you will draw 1 new card for each card discarded',
      items: game.active.hand,
      limit: :one_to_many,
      commands: {
        unable: lambda do
          puts 'no card to discard'
        end,
        discard: lambda do |indexes|
          discarded_cards = game.active.move_many indexes, :hand, :to_discard
          puts "#{game.active.name} discarded #{discarded_cards.join(', ')}"
          drawn_cards = game.active.draw_cards discarded_cards.length
          puts "#{game.active.name} drawed #{drawn_cards.length} cards"
        end
      }
    )
  end
end

class Chancellor < Card
  include ActionCard

  def initialize
    @costs = 3
  end

  def play(game)
    game.coins += 2

    game.active.choose(
      message: 'discard your full deck now?',
      commands: {
        discard: lambda do |_|
          game.active.discard_deck
          puts "#{game.active.name} discarded his full deck"
        end,
        no: lambda do |_|
        end
      }
    )
  end
end

class Chapel < Card
  include ActionCard

  def initialize
    @costs = 2
  end

  def play(game)
    game.active.choose(
      message: 'select up to 4 cards to trash',
      items: game.active.hand,
      limit: [:upto, 4],
      commands: {
        unable: lambda do
          puts 'no card to trash'
        end,
        trash: lambda do |indexes|
          trashed_cards = game.active.move_many indexes, :hand, [:to_trash, game.board]
          puts "#{game.active.name} trashed #{trashed_cards.join(', ')}"
        end
      }
    )
  end
end

class CouncilRoom < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    drawn_cards = game.active.draw_cards 4
    puts "#{game.active.name} drawn #{drawn_cards.length} cards"
    game.buys += 1

    game.active.opponents(game).each do |player|
      drawn_cards = player.draw_cards 1
      puts "#{player.name} drawn #{drawn_cards.length} cards"
    end
  end
end

class Feast < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    my_index = game.active.played.index(self)
    unless my_index.nil?
      game.active.move_one my_index, :played, [:to_trash, game.board]
      puts "#{game.active.name} trashed #{self}"
    end

    game.active.choose(
      message: 'select a pile to gain',
      items: game.board.piles,
      each: ->(pile) { !pile.empty? && pile.sample.costs <= 5 },
      limit: :one,
      commands: {
        unable: lambda do
          puts 'no pile available to gain'
        end,
        gain: lambda do |index|
          gained_card = game.active.gain_from_pile(game.board.piles[index])
          puts "gained #{gained_card}"
        end
      }
    )
  end
end

class Festival < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    game.actions += 2
    game.buys += 1
    game.coins += 2
  end
end

class Gardens < Card
  include VictoryCard, ScoringCard

  def initialize
    @costs = 4
  end

  def score(stat)
    stat.all_cards.length / 10
  end
end

class Laboratory < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    game.actions += 1
    drawn_cards = game.active.draw_cards 2
    puts "#{game.active.name} drawed #{drawn_cards.length} cards"
  end
end

class Library < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    cards_setasided = []
    while game.active.hand.length < 7
      cards_drawn = game.active.draw_cards 1, target: nil
      if cards_drawn.empty?
        break
      elsif cards_drawn.any? { |c| c.is_a? ActionCard }
        actions, non_actions = cards_drawn.partition { |x| x.is_a? ActionCard }
        game.active.choose(
          message: "would you like to discard #{actions.join(', ')}?",
          commands: {
            discard: lambda do |_|
              cards_setasided.concat actions
              game.active.to_hand(non_actions) unless non_actions.empty?
            end,
            no: lambda do |_|
              game.active.to_hand(cards_drawn)
            end
          }
        )
      else
        game.active.to_hand(cards_drawn)
      end
    end

    unless cards_setasided.empty?
      game.active.to_discard(cards_setasided)
      puts "#{game.active.name} discarded #{cards_setasided.join(', ')}"
    end
  end
end

class Market < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    drawn_cards = game.active.draw_cards 1
    puts "#{game.active.name} drawed #{drawn_cards.length} cards"
    game.actions += 1
    game.buys += 1
    game.coins += 1
  end
end

class Militia < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    game.coins += 2

    game.active.opponents(game).each do |player|
      next if player.defend?

      while player.hand.length > 3
        to_discard_length = player.hand.length - 3
        player.choose(
          message: "select #{to_discard_length} cards to discard",
          items: player.hand,
          limit: [:exactly, to_discard_length],
          commands: {
            unable: lambda do
            end,
            discard: lambda do |indexes|
              discarded_cards = player.move_many indexes, :hand, :to_discard
              puts "#{player.name} discarded #{discarded_cards.join(', ')}"
            end
          }
        )
      end
    end
  end
end

class Mine < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    game.active.choose(
      message: 'select a treasure card to trash',
      items: game.active.hand,
      each: ->(c) { c.is_a? TreasureCard },
      limit: :one,
      commands: {
        unable: lambda do
          puts 'no treasure card to trash'
        end,
        trash: lambda do |index|
          trashed_card = game.active.move_one index, :hand, [:to_trash, game.board]
          puts "#{game.active.name} trashed #{trashed_card}"

          game.active.choose(
            message: 'select a pile to gain',
            items: game.board.piles,
            each: ->(pile) { !pile.empty? && pile.sample.is_a?(TreasureCard) && pile.sample.costs <= trashed_card.costs + 3 },
            limit: :one,
            command: {
              unable: lambda do
                puts 'no pile available to gain'
              end,
              gain: lambda do |gain_index|
                gained_card = game.active.gain_from_pile(game.board.piles[gain_index], target: :hand)
                puts "gained #{gained_card}"
              end
            }
          )
        end
      }
    )
  end
end

class Moat < Card
  include ActionCard

  def initialize
    @costs = 2
  end

  def play(game)
    drawn_cards = game.active.draw_cards 2
    puts "#{game.active.name} drawn #{drawn_cards.length} cards"
  end
end

class Moneylender < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    copper_index = game.active.hand.index { |c| c.is_a? Copper }
    unless copper_index.nil?
      trashed_copper = game.active.move_one copper_index, :hand, [:to_trash, game.board]
      puts "#{game.active.name} trashed #{trashed_copper}"
      game.coins += 3
    end
  end
end

class Remodel < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    game.active.choose(
      message: 'select a card to trash',
      items: game.active.hand,
      limit: :one,
      commands: {
        unable: lambda do
          puts 'no card in hand to trash'
        end,
        trash: lambda do |index|
          trashed_card = game.active.move_one(index, :hand, [:to_trash, game.board])
          puts "trashed #{trashed_card}"

          game.active.choose(
            message: 'select a pile to gain',
            items: game.board.piles,
            each: ->(pile) { !pile.empty? && pile.sample.costs <= trashed_card.costs + 2 },
            limit: :one,
            commands: {
              unable: lambda do
                puts 'no pile available to gain'
              end,
              gain: lambda do |gain_index|
                gained_card = game.active.gain_from_pile(game.board.piles[gain_index])
                puts "gained #{gained_card}"
              end
            }
          )
        end
      }
    )
  end
end

class Smithy < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    cards_drawn = game.active.draw_cards 3
    puts "drawn #{cards_drawn.length} cards"
  end
end

class Spy < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    cards_drawn = game.active.draw_cards 1
    puts "drawn #{cards_drawn.length} cards"
    game.actions += 1

    [game.active, *game.active.opponents(game)].each do |player|
      next if player != game.active && player.defend?

      cards_drawn = player.draw_cards 1, target: nil
      game.active.choose(
        message: "select the card to put back or discard for #{player.name}",
        items: cards_drawn,
        limit: :one,
        commands: {
          unable: lambda do
          end,
          putback: lambda do |_|
            player.to_deck cards_drawn
            puts "#{player.name} put back #{cards_drawn.join(', ')}"
          end,
          discard: lambda do |_|
            player.to_discard cards_drawn
            puts "#{player.name} discarded #{cards_drawn.join(', ')}"
          end
        }
      )
    end
  end
end

class Thief < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    game.active.opponents(game).each do |player|
      next if player.defend?

      cards_drawn = player.draw_cards 2, target: nil
      game.active.choose(
        message: "select a treasure card to gain or trash for #{player.name}",
        items: cards_drawn,
        each: ->(c) { c.is_a? TreasureCard },
        limit: :one,
        commands: {
          unable: lambda do
            puts "#{player.name} revealed no treasure card"
          end,
          gain: lambda do |index|
            selected_card = cards_drawn.delete_at index
            game.active.gain_one selected_card
            puts "#{game.active.name} gained #{player.name}'s #{selected_card}"
          end,
          trash: lambda do |index|
            selected_card = cards_drawn.delete_at index
            player.to_trash game.board, [selected_card]
            puts "#{player.name} trashed #{selected_card}"
          end
        }
      )

      unless cards_drawn.empty?
        player.to_discard cards_drawn
        puts "#{player.name} discarded #{cards_drawn.join(', ')}"
      end
    end
  end
end

class ThroneRoom < Card
  include ActionCard

  def initialize
    @costs = 4
  end

  def play(game)
    game.active.choose(
      message: 'select an action card to play',
      items: game.active.hand,
      each: ->(c) { c.is_a? ActionCard },
      limit: :one,
      commands: {
        unable: lambda do
          puts 'no action card in hand to play twice'
        end,
        play: lambda do |index|
          action_card = game.active.move_one index, :hand, :to_played
          puts "playing #{action_card} first time"
          action_card.play(game)
          puts "playing #{action_card} second time"
          action_card.play(game)
        end
      }
    )
  end

  def to_s
    'Throne Room'
  end
end

class Village < Card
  include ActionCard

  def initialize
    @costs = 3
  end

  def play(game)
    cards_drawn = game.active.draw_cards 1
    puts "drawn #{cards_drawn.length} cards"
    game.actions += 2
  end
end

class Witch < Card
  include ActionCard

  def initialize
    @costs = 5
  end

  def play(game)
    cards_drawn = game.active.draw_cards 2
    puts "drawn #{cards_drawn.length} cards"

    game.active.opponents(game).each do |player|
      next if player.defend?

      curse_pile = game.board.piles.find { |x| x.sample.is_a? Curse }
      unless curse_pile.empty?
        gained_curse = player.gain_from_pile curse_pile
        puts "#{player.name} gained a #{gained_curse}"
      end
    end
  end
end

class Woodcutter < Card
  include ActionCard

  def initialize
    @costs = 3
  end

  def play(game)
    game.buys += 1
    game.coins += 2
  end
end

class Workshop < Card
  include ActionCard

  def initialize
    @costs = 3
  end

  def play(game)
    game.active.choose(
      message: 'select a pile to gain',
      items: game.board.piles,
      each: ->(pile) { !pile.empty? && pile.sample.costs <= 4 },
      limit: :one,
      commands: {
        unable: lambda do
          puts 'no pile available to gain'
        end,
        gain: lambda do |index|
          gained_card = game.active.gain_from_pile(game.board.piles[index])
          puts "gained #{gained_card}"
        end
      }
    )
  end
end
