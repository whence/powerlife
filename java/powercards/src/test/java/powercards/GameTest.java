package powercards;

import org.junit.Before;
import org.junit.Test;
import powercards.cards.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameTest {
  private RecordedInputOutput inout;
  private Dialog dialog;

  @Before
  public void setUp() {
    inout = new RecordedInputOutput();
    dialog = new Dialog(inout);
  }

  @Test
  public void shouldInitializeGame() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);

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
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.play();
    assertThat(game.getStage(), is(Stage.TREASURE));
    assertThat(inout.hasOutputs(Arrays.asList("Skip to treasure stage")), is(true));
  }

  @Test
  public void shouldPlayActionCard() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.getActivePlayer().getHand().clear();
    DummyActionCard actionCard = mock(DummyActionCard.class);
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), actionCard, new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.getInputQueue().add("2");

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
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.getActivePlayer().setActions(0);
    assertThat(game.getStage(), is(Stage.ACTION));
    game.play();
    assertThat(game.getStage(), is(Stage.TREASURE));
  }

  @Test
  public void shouldPlayTreasureCards() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.getInputQueue().add("0, 2, 4");

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
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().getHand().addAll(Arrays.asList(new Estate(), new Province()));
    game.play();
    assertThat(game.getStage(), is(Stage.BUY));
  }

  @Test
  public void shouldBuy() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.setStage(Stage.BUY);
    game.getBoard().getPiles().clear();
    game.getBoard().getPiles().addAll(Arrays.asList(
        new Pile(Copper::new, 10), new Pile(Estate::new, 8), new Pile(Province::new, 8),
        new Pile(ThroneRoom::new, 10), new Pile(Remodel::new, 10)));
    game.getActivePlayer().getHand().clear();
    game.getActivePlayer().setCoins(5);
    inout.getInputQueue().add("4");

    assertThat(game.getActivePlayer().getBuys(), is(1));
    assertThat(game.getActivePlayer().getPlayed().size(), is(0));

    game.play();

    assertThat(inout.hasOutputs(Arrays.asList("Bought Remodel")), is(true));

    assertThat(game.getActivePlayer().getDiscard().size(), is(1));
    assertThat(game.getActivePlayer().getDiscard().get(0) instanceof Remodel, is(true));
    assertThat(game.getActivePlayer().getCoins(), is(1));
    assertThat(game.getActivePlayer().getBuys(), is(0));
    assertThat(game.getBoard().getPile(p -> p.getSample() instanceof Remodel).size(), is(9));
    assertThat(game.getStage(), is(Stage.BUY));
  }

  @Test
  public void shouldSkipToCleanupIfNoBuys() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.setStage(Stage.BUY);
    game.getActivePlayer().setBuys(0);
    game.play();
    assertThat(game.getStage(), is(Stage.CLEANUP));
  }

  @Test
  public void shouldPlayRemodel() {
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.getBoard().getPiles().clear();
    game.getBoard().getPiles().addAll(Arrays.asList(
        new Pile(Copper::new, 10), new Pile(Estate::new, 8), new Pile(ThroneRoom::new, 4)));
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Estate(), new Remodel(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    inout.getInputQueue().add("1");
    inout.getInputQueue().add("2");
    inout.getInputQueue().add("2");

    game.play();

    assertThat(inout.hasOutputs(Arrays.asList("Trashed Estate", "Gained Throne Room")), is(true));

    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(1))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(
        hand.get(0), hand.get(2), hand.get(4))));
    assertThat(game.getActivePlayer().getDiscard().size(), is(1));
    assertThat(game.getActivePlayer().getDiscard().get(0) instanceof ThroneRoom, is(true));
    assertThat(game.getBoard().getTrash(), is(Arrays.asList(hand.get(3))));
    assertThat(game.getBoard().getPile(p -> p.getSample() instanceof ThroneRoom).size(), is(3));
  }
}
