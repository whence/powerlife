package powercards;

import org.junit.Test;
import powercards.cards.Copper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class PileTest {
  @Test
  public void shouldPushAndPop() {
    Pile pile = new Pile(Copper::new, 8);

    assertThat(pile.isEmpty(), is(false));

    for (int i = 1; i <= 5; i++) {
      pile.pop();
      assertThat(pile.size(), is(8 - i));
    }

    for (int i = 1; i <= 3; i++) {
      pile.push(new Copper());
      assertThat(pile.size(), is(3 + i));
    }

    for (int i = 1; i <= 6; i++) {
      pile.pop();
      assertThat(pile.size(), is(6 - i));
    }

    assertThat(pile.isEmpty(), is(true));

    for (int i = 1; i <= 2; i++) {
      pile.push(new Copper());
      assertThat(pile.size(), is(i));
    }

    assertThat(pile.isEmpty(), is(false));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfPopEmptyPile() {
    Pile pile = new Pile(Copper::new, 1);
    pile.pop();
    assertThat(pile.isEmpty(), is(true));
    pile.pop();
  }

  @Test
  public void shouldPopDifferentCards() {
    Pile pile = new Pile(Copper::new, 2);
    Card card1 = pile.pop();
    Card card2 = pile.pop();

    assertThat(card1, not(pile.getSample()));
    assertThat(card1, not(card2));
    assertThat(card1 == card2, is(false));
  }

  @Test
  public void shouldPopTheSameCardAsPushed() {
    Pile pile = new Pile(Copper::new, 2);
    Card card1 = new Copper();
    pile.push(card1);
    Card card2 = pile.pop();
    assertThat(card1, is(card2));
    assertThat(card1 == card2, is(true));
  }
}
