require_relative 'pile'
require_relative 'cardlib'

class Board
  attr_reader :trash, :piles

  def initialize(kingdom_card_factories, num_of_players)
    @trash = []

    kingdoms = kingdom_card_factories.map { |cf| Pile.new(cf, 10) }
    kingdoms.sort! { |pile| pile.sample.costs }

    treasures = [Pile.new(Copper, 60 - num_of_players * 7),
                 Pile.new(Silver, 40),
                 Pile.new(Gold, 30)]
    num_of_victories = case num_of_players
                       when 2 then 8
                       when 3, 4 then 12
                       end
    victories = [Pile.new(Estate, num_of_victories),
                 Pile.new(Duchy, num_of_victories),
                 Pile.new(Province, num_of_victories)]
    curses = [Pile.new(Curse, case num_of_players
                              when 2 then 10
                              when 3 then 20
                              when 4 then 30
                              end)]

    @piles = treasures + victories + curses + kingdoms
  end
end
