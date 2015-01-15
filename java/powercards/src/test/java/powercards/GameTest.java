package powercards;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GameTest {

  @Mock
  private Dialog dialog;

  @InjectMocks
  private Game game;

  @Before
  public void setUp() {

  }

  @Test
  public void shouldInitializeGame() {

  }
}
