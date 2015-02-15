package powercards;

import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Duchy;
import powercards.cards.Estate;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertSame;

public class BoardTest {
  @Test
  public void shouldGetPile() {
    List<Pile> piles = Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8));
    Board board = new Board(piles);
    assertSame(piles.get(1), board.getPile(Estate.class));
  }

  @Test(expected = NoSuchElementException.class)
  public void shouldThrowIfGetPileNotExists() {
    new Board(Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8))).getPile(Duchy.class);
  }
}
