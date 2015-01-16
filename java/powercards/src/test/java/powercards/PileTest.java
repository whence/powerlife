package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class PileTest {
  @Test
  public void shouldPushAndPop() {
    Pile pile = new Pile(Copper.class, 8);

    assertThat(pile.isEmpty(), is(false));

    IntStream.rangeClosed(1, 5).forEach(n -> {
      pile.pop();
      assertThat(pile.size(), is(8 - n));
    });

    IntStream.rangeClosed(1, 3).forEach(n -> {
      pile.push(new Copper());
      assertThat(pile.size(), is(3 + n));
    });

    IntStream.rangeClosed(1, 6).forEach(n -> {
      pile.pop();
      assertThat(pile.size(), is(6 - n));
    });

    assertThat(pile.isEmpty(), is(true));

    IntStream.rangeClosed(1, 2).forEach(n -> {
      pile.push(new Copper());
      assertThat(pile.size(), is(n));
    });

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
