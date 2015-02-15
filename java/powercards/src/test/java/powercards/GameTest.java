package powercards;

import org.junit.Before;
import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;
import powercards.cards.Gold;
import powercards.cards.Province;
import powercards.cards.Remodel;
import powercards.cards.Silver;
import powercards.cards.Smithy;
import powercards.cards.ThroneRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameTest {
  private RecordedInputOutput inout;
  private Game game;

  @Before
  public void setUp() {
    inout = new RecordedInputOutput();
    inout.queueShuffle(Collections::shuffle); // for wes init
    inout.queueShuffle(Collections::shuffle); // for bec init
    game = new Game(Arrays.asList("wes", "bec"), new Dialog(inout));
  }

  @Test
  public void shouldInitializeGame() {
    assertEquals(Arrays.asList("wes", "bec"), game.getPlayers().stream().map(Player::getName)
            .collect(Collectors.toList()));

    for (Player player : game.getPlayers()) {
      assertEquals(5, player.getDeck().size());
      assertEquals(5, player.getHand().size());
      assertEquals(0, player.getPlayed().size());
      assertEquals(0, player.getDiscard().size());

      List<Card> fullDeck = new ArrayList<>();
      fullDeck.addAll(player.getDeck());
      fullDeck.addAll(player.getHand());
      assertEquals(7, fullDeck.stream().filter(c -> c instanceof Copper).count());
      assertEquals(3, fullDeck.stream().filter(c -> c instanceof Estate).count());
      assertFalse(player.getHand().stream().anyMatch(c -> c instanceof ActionCard));

      if (player == game.getActivePlayer()) {
        assertEquals(1, player.getActions());
        assertEquals(1, player.getBuys());
      } else {
        assertEquals(0, player.getActions());
        assertEquals(0, player.getBuys());
      }
      assertEquals(0, player.getCoins());
    }

    assertEquals(0, game.getBoard().getTrash().size());
    assertEquals(Stage.ACTION, game.getStage());
  }

  @Test
  public void firstPlayShouldSkipToTreasureStage() {
    game.play();
    assertEquals(Stage.TREASURE, game.getStage());
    assertTrue(inout.hasOutputs("Skip to treasure stage"));
  }

  @Test
  public void shouldPlayActionCard() {
    game.getActivePlayer().getHand().clear();
    TestActionCard actionCard = new TestActionCard() {
      @Override
      public void play(Game game) {
        game.getDialog().inout().output("playing test action card");
      }

      @Override
      public int getCost(Game game) {
        return 0;
      }
    };
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), actionCard, new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.queueInputs("2");

    assertEquals(1, game.getActivePlayer().getActions());
    assertEquals(0, game.getActivePlayer().getPlayed().size());

    game.play();

    assertEquals(0, game.getActivePlayer().getActions());
    assertEquals(Arrays.asList(hand.get(2)), game.getActivePlayer().getPlayed());
    assertEquals(Arrays.asList(hand.get(0), hand.get(1), hand.get(3), hand.get(4)), game.getActivePlayer().getHand());
    assertEquals(Stage.ACTION, game.getStage());
    assertTrue(inout.hasOutputs("playing test action card"));
  }

  @Test
  public void shouldSkipToTreasureIfNoActions() {
    game.getActivePlayer().setActions(0);
    assertEquals(Stage.ACTION, game.getStage());
    game.play();
    assertEquals(Stage.TREASURE, game.getStage());
  }

  @Test
  public void shouldPlayTreasureCards() {
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.queueInputs("0, 2, 4");

    assertEquals(0, game.getActivePlayer().getCoins());
    assertEquals(0, game.getActivePlayer().getPlayed().size());

    game.play();

    assertEquals(3, game.getActivePlayer().getCoins());
    assertEquals(Arrays.asList(hand.get(0), hand.get(2), hand.get(4)), game.getActivePlayer().getPlayed());
    assertEquals(Arrays.asList(hand.get(1), hand.get(3)), game.getActivePlayer().getHand());
    assertEquals(Stage.TREASURE, game.getStage());
  }

  @Test
  public void shouldSkipToBuyIfNoTreasureCards() {
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().getHand().addAll(Arrays.asList(new Estate(), new Province()));
    game.play();
    assertEquals(Stage.BUY, game.getStage());
  }

  @Test
  public void shouldBuy() {
    game.setStage(Stage.BUY);
    game.getBoard().getPiles().clear();
    game.getBoard().getPiles().addAll(Arrays.asList(
        new Pile(Copper::new, 10), new Pile(Estate::new, 8), new Pile(Province::new, 8),
        new Pile(ThroneRoom::new, 10), new Pile(Remodel::new, 10)));
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().setCoins(5);
    inout.queueInputs("4");

    assertEquals(1, game.getActivePlayer().getBuys());
    assertEquals(0, game.getActivePlayer().getPlayed().size());

    game.play();

    assertTrue(inout.hasOutputs("Bought Remodel"));

    assertEquals(1, game.getActivePlayer().getDiscard().size());
    assertTrue(game.getActivePlayer().getDiscard().get(0) instanceof Remodel);
    assertEquals(1, game.getActivePlayer().getCoins());
    assertEquals(0, game.getActivePlayer().getBuys());
    assertEquals(9, game.getBoard().getPile(Remodel.class).size());
    assertEquals(Stage.BUY, game.getStage());
  }

  @Test
  public void shouldSkipToCleanupIfNoBuys() {
    game.setStage(Stage.BUY);
    game.getActivePlayer().setBuys(0);
    game.play();
    assertEquals(Stage.CLEANUP, game.getStage());
  }

  @Test
  public void shouldCleanup() {
    game.setStage(Stage.CLEANUP);
    Player oldActive = game.getActivePlayer();
    oldActive.getHand().clear();
    oldActive.getHand().add(new Estate());
    oldActive.getPlayed().clear();
    oldActive.getPlayed().addAll(Arrays.asList(new Copper(), new Smithy()));

    assertEquals(0, oldActive.getDiscard().size());

    game.play();

    Player newActive = game.getActivePlayer();

    assertTrue(oldActive != newActive);

    assertEquals(0, oldActive.getActions());
    assertEquals(0, oldActive.getBuys());
    assertEquals(0, oldActive.getCoins());
    assertEquals(0, oldActive.getPlayed().size());
    assertEquals(3, oldActive.getDiscard().size());

    assertEquals(1, newActive.getActions());
    assertEquals(1, newActive.getBuys());
    assertEquals(0, newActive.getCoins());
    assertEquals(0, newActive.getPlayed().size());
  }

  @Test
  public void shouldPlayRemodel() {
    game.getBoard().getPiles().clear();
    game.getBoard().getPiles().addAll(Arrays.asList(
        new Pile(Copper::new, 10), new Pile(Estate::new, 8), new Pile(ThroneRoom::new, 4)));
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Estate(), new Remodel(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.queueInputs("1", "2", "2");

    game.play();

    assertTrue(inout.hasOutputs("Trashed Estate", "Gained Throne Room"));

    assertEquals(Arrays.asList(hand.get(1)), game.getActivePlayer().getPlayed());
    assertEquals(Arrays.asList(hand.get(0), hand.get(2), hand.get(4)), game.getActivePlayer().getHand());
    assertEquals(1, game.getActivePlayer().getDiscard().size());
    assertTrue(game.getActivePlayer().getDiscard().get(0) instanceof ThroneRoom);
    assertEquals(Arrays.asList(hand.get(3)), game.getBoard().getTrash());
    assertEquals(3, game.getBoard().getPile(ThroneRoom.class).size());
  }

  @Test
  public void shouldThroneRoomThroneRoomSmithy() {
    game.getBoard().getPiles().clear();
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().getHand().addAll(Arrays.asList(new Smithy(), new ThroneRoom(), new Smithy(),
        new ThroneRoom(), new Copper()));
    game.getActivePlayer().getDeck().clear();
    for (int i = 0; i < 4; i++) {
      game.getActivePlayer().getDeck().addAll(Arrays.asList(new Copper(), new Silver(), new Gold()));
    }
    inout.queueInputs("1", "2", "1", "0", "all");

    game.play(); // play actions
    game.play(); // skip to treasure
    game.play(); // play treasures

    assertTrue(inout.hasOutputs("Playing Throne Room first time", "Playing Smithy first time",
        "Playing Smithy second time", "Playing Throne Room second time", "Playing Smithy first time",
        "Playing Smithy second time"));
    assertEquals(0, inout.getInputQueue().size());

    assertEquals(0, game.getActivePlayer().getHand().size());
    assertEquals(0, game.getActivePlayer().getActions());
    assertEquals(25, game.getActivePlayer().getCoins());
    assertEquals(Stage.TREASURE, game.getStage());
  }
}
