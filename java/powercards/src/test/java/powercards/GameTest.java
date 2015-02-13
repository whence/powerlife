package powercards;

import org.junit.Before;
import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;
import powercards.cards.Province;
import powercards.cards.Remodel;
import powercards.cards.ThroneRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    assertThat(game.getPlayers().size(), is(2));
    assertThat(game.getPlayers().stream().map(Player::getName).collect(Collectors.toList()),
        is(Arrays.asList("wes", "bec")));

    for (Player player : game.getPlayers()) {
      assertThat(player.getDeck().size(), is(5));
      assertThat(player.getHand().size(), is(5));
      assertThat(player.getPlayed().size(), is(0));
      assertThat(player.getDiscard().size(), is(0));

      List<Card> fullDeck = new ArrayList<>();
      fullDeck.addAll(player.getDeck());
      fullDeck.addAll(player.getHand());
      assertThat(fullDeck.stream().filter(c -> c instanceof Copper).count(), is(7L));
      assertThat(fullDeck.stream().filter(c -> c instanceof Estate).count(), is(3L));
      assertThat(player.getHand().stream().anyMatch(c -> c instanceof ActionCard), is(false));

      if (player == game.getActivePlayer()) {
        assertThat(player.getActions(), is(1));
        assertThat(player.getBuys(), is(1));
      } else {
        assertThat(player.getActions(), is(0));
        assertThat(player.getBuys(), is(0));
      }
      assertThat(player.getCoins(), is(0));
    }

    assertThat(game.getBoard().getTrash().size(), is(0));
    assertThat(game.getStage(), is(Stage.ACTION));
  }

  @Test
  public void firstPlayShouldSkipToTreasureStage() {
    game.play();
    assertThat(game.getStage(), is(Stage.TREASURE));
    assertTrue(inout.hasOutputs("Skip to treasure stage"));
  }

  @Test
  public void shouldPlayActionCard() {
    game.getActivePlayer().getHand().clear();
    DummyActionCard actionCard = mock(DummyActionCard.class);
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), actionCard, new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.queueInputs("2");

    assertThat(game.getActivePlayer().getActions(), is(1));
    assertThat(game.getActivePlayer().getPlayed().size(), is(0));

    game.play();

    verify(actionCard).play(game);

    assertThat(game.getActivePlayer().getActions(), is(0));
    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(2))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(
        hand.get(0), hand.get(1), hand.get(3), hand.get(4))));
    assertThat(game.getStage(), is(Stage.ACTION));
  }

  @Test
  public void shouldSkipToTreasureIfNoActions() {
    game.getActivePlayer().setActions(0);
    assertThat(game.getStage(), is(Stage.ACTION));
    game.play();
    assertThat(game.getStage(), is(Stage.TREASURE));
  }

  @Test
  public void shouldPlayTreasureCards() {
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.queueInputs("0, 2, 4");

    assertThat(game.getActivePlayer().getCoins(), is(0));
    assertThat(game.getActivePlayer().getPlayed().size(), is(0));

    game.play();

    assertThat(game.getActivePlayer().getCoins(), is(3));
    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(0), hand.get(2), hand.get(4))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(hand.get(1), hand.get(3))));
    assertThat(game.getStage(), is(Stage.TREASURE));
  }

  @Test
  public void shouldSkipToBuyIfNoTreasureCards() {
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().getHand().addAll(Arrays.asList(new Estate(), new Province()));
    game.play();
    assertThat(game.getStage(), is(Stage.BUY));
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

    assertThat(game.getActivePlayer().getBuys(), is(1));
    assertThat(game.getActivePlayer().getPlayed().size(), is(0));

    game.play();

    assertTrue(inout.hasOutputs("Bought Remodel"));

    assertThat(game.getActivePlayer().getDiscard().size(), is(1));
    assertThat(game.getActivePlayer().getDiscard().get(0) instanceof Remodel, is(true));
    assertThat(game.getActivePlayer().getCoins(), is(1));
    assertThat(game.getActivePlayer().getBuys(), is(0));
    assertThat(game.getBoard().getPile(p -> p.getSample() instanceof Remodel).size(), is(9));
    assertThat(game.getStage(), is(Stage.BUY));
  }

  @Test
  public void shouldSkipToCleanupIfNoBuys() {
    game.setStage(Stage.BUY);
    game.getActivePlayer().setBuys(0);
    game.play();
    assertThat(game.getStage(), is(Stage.CLEANUP));
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

    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(1))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(
        hand.get(0), hand.get(2), hand.get(4))));
    assertThat(game.getActivePlayer().getDiscard().size(), is(1));
    assertThat(game.getActivePlayer().getDiscard().get(0) instanceof ThroneRoom, is(true));
    assertThat(game.getBoard().getTrash(), is(Arrays.asList(hand.get(3))));
    assertThat(game.getBoard().getPile(p -> p.getSample() instanceof ThroneRoom).size(), is(3));
  }
}
