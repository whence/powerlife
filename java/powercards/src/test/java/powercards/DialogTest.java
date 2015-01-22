package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
  }

  @Test
  public void shouldChooseOne() {
    when(inputOutput.input()).thenReturn("2");
    assertThat(dialog.chooseOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", true), new Choice("2", true))
    ), is(OptionalInt.of(2)));
  }

  @Test
  public void shouldChooseOptionalOne() {
    when(inputOutput.input()).thenReturn("1");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", false))
    ), is(OptionalInt.of(1)));
  }

  @Test
  public void shouldSkipOptionalOne() {
    when(inputOutput.input()).thenReturn("skip");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", true))
    ).isPresent(), is(false));
  }

  @Test
  public void shouldChooseUnlimited() {
    when(inputOutput.input()).thenReturn("1, 2");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2 }));
  }

  @Test
  public void shouldChooseUnlimitedWithAll() {
    when(inputOutput.input()).thenReturn("1,2, 0,");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2, 0 }));
  }

  @Test
  public void shouldSkipUnlimited() {
    when(inputOutput.input()).thenReturn("skip");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void shouldSkipUnlimitedWithEmptyString() {
    when(inputOutput.input()).thenReturn("  ");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void shouldReturnNotPresentWhenUnableToChoose() {
    assertThat(dialog.chooseOne("", new ArrayList<>()).isPresent(), is(false));
    assertThat(dialog.chooseOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent(), is(false));

    assertThat(dialog.chooseOptionalOne("", new ArrayList<>()).isPresent(), is(false));
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent(), is(false));

    assertThat(dialog.chooseUnlimited("", new ArrayList<>()).isPresent(), is(false));
    assertThat(dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent(), is(false));

    verify(inputOutput, never()).output(anyString());
    verify(inputOutput, never()).input();
  }
}
