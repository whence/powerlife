package powercards;

import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

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
    assertNotSame(a, b);
    assertEquals(a, b);

    List<Card> c = Arrays.asList(card3, card4);
    List<Card> d = Arrays.asList(card3, card4);
    assertNotSame(c, d);
    assertEquals(c, d);

    List<Card> e = new ArrayList<>();
    e.add(card1);
    e.add(card3);
    List<Card> f = Arrays.asList(card1, card3);
    assertEquals(e, f);
    assertEquals(f, e);
  }

  @Test
  public void stringShouldWorkWithAssert() {
    String a = "wes";
    String b = "WES".toLowerCase();
    assertNotSame(a, b);
    assertEquals(a, b);
  }

  @Test
  public void optionalIntShouldWorkWithAssert() {
    OptionalInt a = OptionalInt.of(1);
    OptionalInt b = OptionalInt.of(1);
    assertNotSame(a, b);
    assertEquals(a, b);
  }
}
