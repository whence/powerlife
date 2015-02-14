package powercards;

import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BoardTest {
  @Test
  public void shouldGetPile() {
    Board board = new Board(Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8)));
    assertThat(board.getPile(p -> p.getSample() instanceof Estate), is(board.getPiles().get(1)));
  }
}
