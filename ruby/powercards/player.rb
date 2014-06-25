require_relative 'cardlib'
require_relative 'client'
require_relative 'utils'

class Player
  attr_reader :name
  attr_reader :deck, :hand, :played, :discard

  def initialize(name)
    @name = name
    deck = Array.new(3) { Estate.new } + Array.new(7) { Copper.new }
    deck.shuffle!
    @deck, @hand = deck.first(5), deck.last(5)
    @played, @discard = [], []
    @client = Client.new
  end

  def choose(message: '', items: [],
             each: ->(_) { true }, limit: :unlimited,
             commands: {})
    limitor, argument = limit
    case limitor
    when :one, :one_to_many
      if commands.key?(:autoplay) && items.count(&each) == 1
        return commands[:autoplay].call(items.find_index(&each))
      elsif !items.any?(&each)
        return commands[:unable].call
      end
    when :upto
      if argument <= 0
        fail 'argument must be positive'
      elsif argument == 1 && commands.key?(:autoplay) && !items.any?(&each)
        return commands[:autoplay].call([])
      end
    when :exactly
      if argument <= 0
        return commands[:unable].call
      elsif argument == 1 && commands.key?(:autoplay) && items.count(&each) == 1
        return commands[:autoplay].call(items.find_index(&each))
      elsif !items.any?(&each)
        return commands[:unable].call
      elsif commands.key?(:autoplay) && items.count(&each) <= argument
        return commands[:autoplay].call(Utils.find_all_indexes(items, &each))
      end
    end

    req = { name: @name,
            message: message,
            items: items.map do |item|
              { name: item.to_s,
                selectable: each.call(item) }
            end,
            limit: limit,
            commands: commands.keys }

    command, argument = @client.ask req
    if command == :skip
      commands[command].call
    elsif limitor == :one
      commands[command].call(argument.first)
    else
      commands[command].call(argument)
    end
  end

  def move_one(index, from, to)
    src = send(*from)
    card = src.delete_at index
    send(*to, [card])
    card
  end

  def move_many(indexes, from, to)
    src = send(*from)
    indexes_rev = (0...src.length).to_a - indexes
    cards = indexes.map { |i| src[i] }
    cards_rev = indexes_rev.map { |i| src[i] }
    src.replace cards_rev
    send(*to, cards)
    cards
  end

  def gain_from_pile(pile, target: :discard)
    card = pile.pop
    send(target) << card
    card
  end

  def gain_one(card, target: :discard)
    send(target) << card
    card
  end

  def draw_cards(n, target: :hand)
    target_zone = target.nil? ? [] : send(target)
    if @deck.length >= n
      if n == 1
        card = @deck.pop
        target_zone << card
        [card]
      else
        cards = @deck.last(n).reverse
        target_zone.concat cards
        @deck = @deck.first(@deck.length - n)
        cards
      end
    elsif @discard.empty?
      cards = @deck.reverse
      target_zone.concat cards
      @deck = []
      cards
    else
      cards = @deck.reverse
      target_zone.concat cards
      @deck = @discard.shuffle
      @discard = []
      cards + draw_cards(n - cards.length, target: target)
    end
  end

  def discard_deck
    @discard.concat @deck.reverse
    @deck = []
  end

  def to_deck(cards)
    @deck.concat cards
  end

  def to_hand(cards)
    @hand.concat cards
  end

  def to_played(cards)
    @played.concat cards
  end

  def to_discard(cards)
    @discard.concat cards
  end

  def to_trash(board, cards)
    board.trash.concat cards
  end

  def cleanup
    @discard.concat @hand
    @discard.concat @played
    @hand = []
    @played = []
    draw_cards 5
  end

  def opponents(game)
    game.players.rotate(game.players.index(self) + 1)[0...-1]
  end

  def defend?
    choose(
      message: 'select a card to defend',
      items: @hand,
      each: ->(c) { c.is_a? Moat },
      limit: :one,
      commands: {
        unable: ->() { false },
        skip: ->() { false },
        defend: lambda do |index|
          puts "#{@name} defended the attack using #{@hand[index]}"
          true
        end
      }
    )
  end
end
