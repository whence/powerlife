package powercards;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;
import powercards.cards.Estate;
import powercards.cards.Remodel;
import powercards.cards.ThroneRoom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class GameTest {

  @Mock
  private InputOutput inputOutput;

  @Before
  public void setUp() {
  }

  @Test
  public void shouldInitializeGame() {
    Game game = new Game(Arrays.asList("wes", "bec"), new Dialog(inputOutput), inputOutput);

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
      } else {
        assertThat(player.getActions(), is(0));
      }
      assertThat(player.getBuys(), is(0));
      assertThat(player.getCoins(), is(0));
    }

    assertThat(game.getBoard().getTrash().size(), is(0));
    assertThat(game.getStage(), is(Stage.ACTION));
  }

  @Test
  public void firstPlayShouldSkipToTreasureStage() {
    Game game = new Game(Arrays.asList("wes", "bec"), new Dialog(inputOutput), inputOutput);
    game.play();
    assertThat(game.getStage(), is(Stage.TREASURE));
    verify(inputOutput).output("Skip to treasure stage");
  }

  @Test
  public void shouldPlayTreasureCards() {
    Game game = new Game(Arrays.asList("wes", "bec"), new Dialog(inputOutput), inputOutput);
    game.setStage(Stage.TREASURE);
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    when(inputOutput.input()).thenReturn("0, 2, 4");

    assertThat(game.getActivePlayer().getCoins(), is(0));
    assertThat(game.getActivePlayer().getPlayed().size(), is(0));

    game.play();

    assertThat(game.getActivePlayer().getCoins(), is(3));
    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(0), hand.get(2), hand.get(4))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(hand.get(1), hand.get(3))));
    assertThat(game.getStage(), is(Stage.TREASURE));
  }

  @Test
  public void shouldPlayActionCard() {
    Game game = new Game(Arrays.asList("wes", "bec"), new Dialog(inputOutput), inputOutput);
    game.getActivePlayer().getHand().clear();
    DummyActionCard actionCard = mock(DummyActionCard.class);
    List<Card> hand = Arrays.asList(new Copper(), new Estate(), actionCard, new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    when(inputOutput.input()).thenReturn("2");

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
  public void shouldPlayRemodel() {
    Game game = new Game(Arrays.asList("wes", "bec"), new Dialog(inputOutput), inputOutput);
    game.getBoard().getPiles().clear();
    game.getBoard().getPiles().addAll(Arrays.asList(
        new Pile(Copper.class, 10), new Pile(Estate.class, 8), new Pile(ThroneRoom.class, 4)));
    game.getActivePlayer().getHand().clear();
    List<Card> hand = Arrays.asList(new Estate(), new Remodel(), new Copper(), new Estate(), new Copper());
    game.getActivePlayer().getHand().addAll(hand);
    when(inputOutput.input()).thenReturn("1").thenReturn("2").thenReturn("2");

    game.play();

    verify(inputOutput).output("Trashed Estate");
    verify(inputOutput).output("Gained Throne Room");

    assertThat(game.getActivePlayer().getPlayed(), is(Arrays.asList(hand.get(1))));
    assertThat(game.getActivePlayer().getHand(), is(Arrays.asList(
        hand.get(0), hand.get(2), hand.get(4))));
    assertThat(game.getActivePlayer().getDiscard().size(), is(1));
    assertThat(game.getActivePlayer().getDiscard().get(0) instanceof ThroneRoom, is(true));
    assertThat(game.getBoard().getTrash(), is(Arrays.asList(hand.get(3))));
    assertThat(game.getBoard().getPile(p -> p.getSample() instanceof ThroneRoom).size(), is(3));
  }
}
