require 'yaml'
require_relative 'game'
require_relative 'cardlib'

game = if ARGV.length == 1
         puts "loading game from #{ARGV[0]}"
         YAML.load_file ARGV[0]
       else
         kingdom_cards = [
           Adventurer, Bureaucrat, Cellar, Chancellor, Chapel,
           CouncilRoom, Feast, Festival, Laboratory, Library,
           Market, Militia, Mine, Moat, Moneylender,
           Remodel, Smithy, Spy, Thief, ThroneRoom,
           Village, Witch, Woodcutter, Workshop, Gardens]
         game = Game.new %w(wes bec), kingdom_cards.sample(10)
       end

loop do
  game_yaml = YAML.dump(game)
  begin
    game.continue
  rescue
    filename = "#{Time.now.strftime('%Y%m%d-%H%M%S')}.yaml"
    File.open(filename, 'w') do |f|
      f.write game_yaml
    end
    puts "error occured. game saved to #{filename}"
    raise
  end
end
# TODO: refactor input/output, and add card tests
