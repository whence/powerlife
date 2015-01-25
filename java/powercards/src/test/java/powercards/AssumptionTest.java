package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class AssumptionTest {
  @Test
  public void arrayListShouldStructuralCompared() {
    Card card1 = new Copper();
    Card card2 = new Estate();
    Card card3 = new Copper();
    Card card4 = new Estate();

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

    List<Card> e = new ArrayList<>();
    e.add(card1);
    e.add(card3);
    assertThat(e, is(Arrays.asList(card1, card3)));
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
  public void optionalIntShouldWorkWithIsOperator() {
    OptionalInt a = OptionalInt.of(1);
    OptionalInt b = OptionalInt.of(1);
    assertThat(a == b, is(false));
    assertThat(a, is(b));
    assertThat(b, is(a));
  }
}
