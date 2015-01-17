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
  public void testCardsShouldComparedWithReference() {
    assertThat(card1, is(not(card3)));
    assertThat(card2, is(not(card4)));
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
  public void shouldMoveMany() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3));
    List<Card> target = new ArrayList<>(Arrays.asList(card4));

    List<Card> cards = Cards.moveMany(source, target, new int[] {0, 2});

    assertThat(source, is(Arrays.asList(card2)));
    assertThat(target, is(Arrays.asList(card4, card1, card3)));
    assertThat(cards, is(Arrays.asList(card1, card3)));
  }

  @Test
  public void shouldInverseIndexes() {
    assertThat(Cards.inverseIndexes(new int[] { 1, 2, 4 }, 6), is(new int[] { 0, 3, 5 }));
  }
}
