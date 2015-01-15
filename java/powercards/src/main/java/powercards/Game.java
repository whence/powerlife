package powercards;

import java.util.List;
import java.util.stream.Collectors;

public class Game {
  private final List<Player> players;

  private final Dialog dialog;

  public List<Player> getPlayers() {
    return players;
  }

  public Game(List<String> playerNames, Dialog dialog) {
    if (playerNames.size() < 2 || playerNames.size() > 4) {
      throw new IllegalArgumentException("there should be only 2-4 players");
    }

    this.players = playerNames.stream().map(Player::new).collect(Collectors.toList());
    this.dialog = dialog;
  }
}
