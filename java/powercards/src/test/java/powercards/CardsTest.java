package powercards;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.CoreMatchers.either;

@RunWith(MockitoJUnitRunner.class)
public class CardsTest {
  private Card card1;
  private Card card2;
  private Card card3;
  private Card card4;
  private Card card5;
  private Card card6;

  @Before
  public void setUp() {
    card1 = new Copper();
    card2 = new Silver();
    card3 = new Gold();
    card4 = new Estate();
    card5 = new Duchy();
    card6 = new Province();
  }

  @Test
  public void cardsShouldReferenceCompare() {
    assertThat(card1, not(card3));
    assertThat(card2, not(card4));
    assertThat(card1 == card3, is(false));
    assertThat(card2 == card4, is(false));
  }

  @Test
  public void shouldMoveOne() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3));
    List<Card> target = new ArrayList<>(Arrays.asList(card4));

    Card card = Cards.moveOne(source, target, 1);

    assertThat(source, is(Arrays.asList(card1, card3)));
    assertThat(target, is(Arrays.asList(card4, card2)));
    assertThat(card, is(card2));
  }

  @Test
  public void shouldMoveOneFromPile() {
    Pile pile = new Pile(Copper.class, 10);
    List<Card> target = new ArrayList<>(Arrays.asList(card1, card2));

    Card card = Cards.moveOne(pile, target);

    assertThat(pile.size(), is(9));
    assertThat(target.size(), is(3));
    assertThat(target, is(Arrays.asList(card1, card2, card)));
  }

  @Test
  public void shouldMoveSome() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3));
    List<Card> target = new ArrayList<>(Arrays.asList(card4));
    int[] sourceIndexes = new int[] { 2, 0 };

    List<Card> cards = Cards.moveMany(source, target, sourceIndexes);

    assertThat(source, is(Arrays.asList(card2)));
    assertThat(target, is(Arrays.asList(card4, card3, card1)));
    assertThat(cards, is(Arrays.asList(card3, card1)));
    assertThat(sourceIndexes, is(new int[] { 2, 0 }));
  }

  @Test
  public void shouldMoveALot() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3, card4));
    List<Card> target = new ArrayList<>();
    int[] sourceIndexes = new int[] { 3, 1 };

    List<Card> cards = Cards.moveMany(source, target, sourceIndexes);

    assertThat(source, is(Arrays.asList(card1, card3)));
    assertThat(target, is(Arrays.asList(card4, card2)));
    assertThat(cards, is(Arrays.asList(card4, card2)));
    assertThat(sourceIndexes, is(new int[] { 3, 1 }));
  }

  @Test
  public void shouldInverseIndexes() {
    int[] original = new int[] { 1, 2, 4 };
    int[] result = Cards.inverseIndexes(original, 6);
    assertThat(result, is(new int[] { 0, 3, 5 }));
    assertThat(original, is(new int[] { 1, 2, 4}));
    assertThat(result == original, is(false));
  }

  @Test
  public void shouldSortIndexesAscending() {
    int[] original = new int[] { 3, 2, 5, 1 };
    int[] result = Cards.sortIndexes(original, false);
    assertThat(result, is(new int[] { 1, 2, 3, 5 }));
    assertThat(original, is(new int[] { 3, 2, 5, 1 }));
    assertThat(result == original, is(false));
  }

  @Test
  public void shouldSortIndexesDescending() {
    int[] original = new int[] { 1, 2, 5, 3 };
    int[] result = Cards.sortIndexes(original, true);
    assertThat(result, is(new int[] { 5, 3, 2, 1 }));
    assertThat(original, is(new int[] { 1, 2, 5, 3 }));
    assertThat(result == original, is(false));
  }

  @Test
  public void shouldCreateNewCard() {
    Copper a = Cards.of(Copper.class);
    Copper b = Cards.of(Copper.class);
    assertThat(a == b, is(false));
  }

  @Test
  public void shouldDrawCards() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);

    List<Card> result = Cards.drawCards(player, 2);

    assertThat(result, is(Arrays.asList(card3, card2)));
    assertThat(player.getDeck(), is(Arrays.asList(card1)));
    assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2)));
  }

  @Test
  public void shouldDrawCardsWhenDeckIsEmpty() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getHand().clear();
    player.getHand().addAll(Arrays.asList(card1, card2));
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card3));

    List<Card> result = Cards.drawCards(player, 1);

    assertThat(result, is(Arrays.asList(card3)));
    assertThat(player.getDeck().size(), is(0));
    assertThat(player.getHand(), is(Arrays.asList(card1, card2, card3)));
    assertThat(player.getDiscard().size(), is(0));
  }

  @Test
  public void shouldDrawCardsFullDeck() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3, card4));
    player.getHand().clear();
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 4);

    assertThat(result, is(Arrays.asList(card4, card3, card2, card1)));
    assertThat(player.getDeck().size(), is(0));
    assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2, card1)));
    assertThat(player.getDiscard(), is(Arrays.asList(card5, card6)));
  }

  @Test
  public void shouldDrawCardsAndRecycleDiscard() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 4);

    assertThat(result.size(), is(4));

    if (result.get(3) == card5) {
      assertThat(result, is(Arrays.asList(card3, card2, card1, card5)));
      assertThat(player.getDeck(), is(Arrays.asList(card6)));
      assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2, card1, card5)));
    } else {
      assertThat(result, is(Arrays.asList(card3, card2, card1, card6)));
      assertThat(player.getDeck(), is(Arrays.asList(card5)));
      assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2, card1, card6)));
    }

    assertThat(player.getDiscard().size(), is(0));
  }

  @Test
  public void shouldDrawAllCardsAndRecycleDiscard() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 5);

    assertThat(result.size(), is(5));

    if (result.get(4) == card5) {
      assertThat(result, is(Arrays.asList(card3, card2, card1, card6, card5)));
      assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2, card1, card6, card5)));
    } else {
      assertThat(result, is(Arrays.asList(card3, card2, card1, card5, card6)));
      assertThat(player.getHand(), is(Arrays.asList(card4, card3, card2, card1, card5, card6)));
    }

    assertThat(player.getDeck().size(), is(0));
    assertThat(player.getDiscard().size(), is(0));
  }

  @Test
  public void shouldDrawCardsButStopIfNoMoreCardsToDraw() {
    Player player = new Player("wes");
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2));
    player.getHand().clear();
    player.getHand().addAll(Arrays.asList(card3, card4));
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 5);

    assertThat(result.size(), is(4));

    if (result.get(3) == card5) {
      assertThat(result, is(Arrays.asList(card2, card1, card6, card5)));
      assertThat(player.getHand(), is(Arrays.asList(card3, card4, card2, card1, card6, card5)));
    } else {
      assertThat(result, is(Arrays.asList(card2, card1, card5, card6)));
      assertThat(player.getHand(), is(Arrays.asList(card3, card4, card2, card1, card5, card6)));
    }

    assertThat(player.getDeck().size(), is(0));
    assertThat(player.getDiscard().size(), is(0));
  }
}
