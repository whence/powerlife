package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.OptionalInt;

import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(MockitoJUnitRunner.class)
public class DialogTest {
  @Mock
  private InputOutput inputOutput;

  @InjectMocks
  private Dialog dialog;

  @Test
  public void optionalIntShouldWorkWithIsOperator() {
    OptionalInt a = OptionalInt.of(1);
    OptionalInt b = OptionalInt.of(1);
    assertThat(a == b, is(false));
    assertThat(a, is(b));
    assertThat(b, is(a));

    OptionalInt c = OptionalInt.empty();
    OptionalInt d = OptionalInt.empty();
    OptionalInt e = OptionalInt.of(0);
    assertThat(c, is(d));
    assertThat(d, is(c));
    assertThat(e, is(not(c)));
  }

  @Test
  public void shouldChooseOne() {
    when(inputOutput.input()).thenReturn("2");
    OptionalInt choice = dialog.chooseOne("blah", Arrays.asList(
        new Choice("0", false), new Choice("1", true), new Choice("2", true)));
    assertThat(choice, is(OptionalInt.of(2)));
  }

  @Test
  public void chooseOneShouldReturnEmptyWhenUnableToChoose() {

  }
}
