package powercards;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
  private final List<Player> players;
  private int activePlayerIndex;
  private Stage stage;

  private final Board board;
  private final Dialog dialog;
  private final InputOutput inputOutput;

  public Game(List<String> playerNames, Dialog dialog, InputOutput inputOutput) {
    if (playerNames.size() < 2 || playerNames.size() > 4) {
      throw new IllegalArgumentException("there should be only 2-4 players");
    }

    this.players = playerNames.stream().map(Player::new).collect(Collectors.toList());

    Random random = new Random();
    this.activePlayerIndex = random.nextInt(players.size());
    this.stage = Stage.ACTION;
    this.board = new Board(Arrays.asList(new Pile(Copper.class, 60), new Pile(Estate.class, 12)));
    this.dialog = dialog;
    this.inputOutput = inputOutput;

    getActivePlayer().setActions(1);
  }

  public List<Player> getPlayers() {
    return players;
  }

  public final Player getActivePlayer() {
    return players.get(activePlayerIndex);
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Board getBoard() {
    return board;
  }

  public void play() {
    switch (stage) {
      case ACTION:
        playAction();
        break;

      case TREASURE:
        playTreasure();
        break;

      case BUY:
        playBuy();
        break;

      case CLEANUP:
        playCleanup();
        break;
    }
  }

  private void playAction() {
    if (getActivePlayer().getActions() == 0) {
      inputOutput.output("No more actions, skip to treasure stage");
      stage = Stage.TREASURE;
    } else {
      OptionalInt idxAction = dialog.chooseOptionalOne("Select an action card to play",
          Cards.toChoices(getActivePlayer().getHand(), c -> c instanceof ActionCard));
      if (idxAction.isPresent()) {
        // todo
      } else {
        inputOutput.output("Skip to treasure stage");
        stage = Stage.TREASURE;
      }
    }
  }

  private void playTreasure() {
    Optional<int[]> idxTreasure = dialog.chooseUnlimited("Select treasure cards to play",
        Cards.toChoices(getActivePlayer().getHand(), c -> c instanceof TreasureCard));
    if (idxTreasure.isPresent()) {
      List<Card> treasures = Cards.moveMany(getActivePlayer().getHand(), getActivePlayer().getPlayed(),
          idxTreasure.get());
      for (Card treasure : treasures) {
        ((TreasureCard) treasure).play(this);
      }
    } else {
      inputOutput.output("Skip to buy stage");
      stage = Stage.BUY;
    }
  }

  private void playBuy() {
  }

  private void playCleanup() {
  }
}
