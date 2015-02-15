package powercards;

import org.junit.Test;
import powercards.cards.Copper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotEquals;

public class PileTest {
  @Test
  public void shouldPushAndPop() {
    Pile pile = new Pile(Copper::new, 8);

    assertEquals(8, pile.size());
    assertFalse(pile.isEmpty());

    for (int i = 1; i <= 5; i++) {
      pile.pop();
      assertEquals(8 - i, pile.size());
    }

    for (int i = 1; i <= 3; i++) {
      pile.push(new Copper());
      assertEquals(3 + i, pile.size());
    }

    for (int i = 1; i <= 6; i++) {
      pile.pop();
      assertEquals(6 - i, pile.size());
    }

    assertTrue(pile.isEmpty());

    for (int i = 1; i <= 2; i++) {
      pile.push(new Copper());
      assertEquals(i, pile.size());
    }

    assertFalse(pile.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfPopEmptyPile() {
    Pile pile = new Pile(Copper::new, 1);
    pile.pop();
    assertTrue(pile.isEmpty());
    pile.pop();
  }

  @Test
  public void shouldPopDifferentCards() {
    Pile pile = new Pile(Copper::new, 2);
    Card card1 = pile.pop();
    Card card2 = pile.pop();

    assertNotEquals(card1, pile.getSample());
    assertNotSame(card1, pile.getSample());
    assertNotEquals(card1, card2);
    assertNotSame(card1, card2);
  }

  @Test
  public void shouldPopTheSameCardAsPushed() {
    Pile pile = new Pile(Copper::new, 2);
    Card card1 = new Copper();
    pile.push(card1);
    Card card2 = pile.pop();
    assertEquals(card1, card2);
    assertSame(card1, card2);
  }
}
