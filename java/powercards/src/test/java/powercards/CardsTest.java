package powercards;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(MockitoJUnitRunner.class)
public class CardsTest {
  private Card card1;
  private Card card2;
  private Card card3;
  private Card card4;

  @Before
  public void setUp() {
    card1 = new Copper();
    card2 = new Estate();
    card3 = new Copper();
    card4 = new Estate();
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
}
