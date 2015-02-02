package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class BoardTest {
  @Test
  public void shouldGetPile() {
    Board board = new Board(Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8)));
    assertThat(board.getPile(p -> p.getSample() instanceof Estate), is(board.getPiles().get(1)));
  }
}
