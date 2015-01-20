package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class PileTest {
  @Test
  public void shouldPushAndPop() {
    Pile pile = new Pile(Copper.class, 8);

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
    Pile pile = new Pile(Copper.class, 1);
    pile.pop();
    assertThat(pile.isEmpty(), is(true));
    pile.pop();
  }
}
