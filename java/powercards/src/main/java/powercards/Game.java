package powercards;

import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Game {
  private final List<Player> players;
  private int activePlayerIndex;

  private final Board board;
  private final Dialog dialog;

  public List<Player> getPlayers() {
    return players;
  }

  public Game(List<String> playerNames, Dialog dialog) {
    if (playerNames.size() < 2 || playerNames.size() > 4) {
      throw new IllegalArgumentException("there should be only 2-4 players");
    }

    this.players = playerNames.stream().map(Player::new).collect(Collectors.toList());

    Random random = new Random();
    this.activePlayerIndex = random.nextInt(players.size());
    this.board = new Board(Arrays.asList(new Pile(Copper.class, 60), new Pile(Estate.class, 12)));
    this.dialog = dialog;
  }

  public Player getActivePlayer() {
    return players.get(activePlayerIndex);
  }
}
