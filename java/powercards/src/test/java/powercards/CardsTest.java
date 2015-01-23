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
  }

  @Test
  public void arrayListShouldStructuralCompared() {
    List<Card> a = new ArrayList<>();
    List<Card> b = new ArrayList<>();
    a.add(card1);
    a.add(card2);
    b.add(card1);
    b.add(card2);
    assertThat(a == b, is(false));
    assertThat(a.equals(b), is(true));
    assertThat(a, is(b));
    assertThat(b, is(a));

    List<Card> c = Arrays.asList(card3, card4);
    List<Card> d = Arrays.asList(card3, card4);
    assertThat(c == d, is(false));
    assertThat(c.equals(d), is(true));
    assertThat(c, is(d));
    assertThat(d, is(c));
  }

  @Test
  public void arrayShouldWorkWithIsOperator() {
    int[] a = new int[] { 1, 2, 3 };
    int[] b = new int[] { 1, 2, 3 };
    assertThat(a == b, is(false));
    assertThat(a, is(b));
    assertThat(b, is(a));
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
  public void shouldConvertToChoices() {
    List<Choice> choices = Cards.toChoices(Arrays.asList(new Copper(), new Estate()), c -> c instanceof VictoryCard);
    assertThat(choices.size(), is(2));
    assertThat(choices.get(0).getName(), is("Copper"));
    assertThat(choices.get(0).isSelectable(), is(false));
    assertThat(choices.get(1).getName(), is("Estate"));
    assertThat(choices.get(1).isSelectable(), is(true));
  }
}
