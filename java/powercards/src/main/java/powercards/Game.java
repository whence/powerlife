package powercards;

import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.*;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
  private final List<Player> players;
  private int activePlayerIndex;
  private Stage stage;

  private final Board board;
  private final Dialog dialog;

  public Game(List<String> playerNames, Dialog dialog) {
    if (playerNames.size() < 2 || playerNames.size() > 4) {
      throw new IllegalArgumentException("there should be only 2-4 players");
    }

    this.players = playerNames.stream().map(Player::new).collect(Collectors.toList());

    this.activePlayerIndex = RandomUtils.nextInt(0, players.size());
    this.stage = Stage.ACTION;
    this.board = new Board(Arrays.asList(new Pile(Copper::new, 60), new Pile(Estate::new, 12)));
    this.dialog = dialog;

    getActivePlayer().setActions(1);
    getActivePlayer().setBuys(1);
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

  public Dialog getDialog() {
    return dialog;
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
      dialog.inout().output("No more actions, skip to treasure stage");
      stage = Stage.TREASURE;
    } else {
      OptionalInt iAction = dialog.chooseOptionalOne("Select an action card to play",
          Choices.ofCards(getActivePlayer().getHand(), c -> c instanceof ActionCard));
      if (iAction.isPresent()) {
        Card actionCard = Cards.moveOne(getActivePlayer().getHand(), getActivePlayer().getPlayed(),
            iAction.getAsInt());
        getActivePlayer().addActions(-1);
        dialog.inout().output("Playing " + actionCard);
        ((ActionCard) actionCard).play(this);
      } else {
        dialog.inout().output("Skip to treasure stage");
        stage = Stage.TREASURE;
      }
    }
  }

  private void playTreasure() {
    Optional<int[]> iTreasure = dialog.chooseUnlimited("Select treasure cards to play",
        Choices.ofCards(getActivePlayer().getHand(), c -> c instanceof TreasureCard));
    if (iTreasure.isPresent()) {
      List<Card> treasureCards = Cards.moveMany(getActivePlayer().getHand(), getActivePlayer().getPlayed(),
          iTreasure.get());
      for (Card treasure : treasureCards) {
        ((TreasureCard) treasure).play(this);
      }
    } else {
      dialog.inout().output("Skip to buy stage");
      stage = Stage.BUY;
    }
  }

  private void playBuy() {
    if (getActivePlayer().getBuys() == 0) {
      dialog.inout().output("No more buys, skip to cleanup stage");
      stage = Stage.CLEANUP;
    } else {
      OptionalInt iBuy = dialog.chooseOptionalOne("Select a pile to buy",
          Choices.ofPiles(getBoard().getPiles(),
              p -> !p.isEmpty() && p.getSample().getCost(this) <= getActivePlayer().getCoins()));
      if (iBuy.isPresent()) {
        Card boughtCard = Cards.moveOne(getBoard().getPiles().get(iBuy.getAsInt()), getActivePlayer().getDiscard());
        dialog.inout().output("Bought " + boughtCard);
        getActivePlayer().addCoins(-boughtCard.getCost(this));
        getActivePlayer().addBuys(-1);
      } else {
        dialog.inout().output("Skip to cleanup stage");
        stage = Stage.CLEANUP;
      }
    }
  }

  private void playCleanup() {
  }
}
