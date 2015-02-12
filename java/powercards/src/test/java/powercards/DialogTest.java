package powercards;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DialogTest {
  private RecordedInputOutput inout;
  private Dialog dialog;

  @Before
  public void setUp() {
    inout = new RecordedInputOutput();
    dialog = new Dialog(inout);
  }

  @Test
  public void shouldChooseOne() {
    inout.getInputQueue().add("2");
    assertThat(dialog.chooseOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", true), new Choice("2", true))
    ), is(OptionalInt.of(2)));
  }

  @Test
  public void shouldChooseOptionalOne() {
    inout.getInputQueue().add("1");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", false))
    ), is(OptionalInt.of(1)));
  }

  @Test
  public void shouldSkipOptionalOne() {
    inout.getInputQueue().add("skip");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", true))
    ).isPresent(), is(false));
  }

  @Test
  public void shouldChooseUnlimited() {
    inout.getInputQueue().add("1, 2");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2 }));
  }

  @Test
  public void shouldChooseUnlimitedWithAll() {
    inout.getInputQueue().add("1,2, 0,");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2, 0 }));
  }

  @Test
  public void shouldChooseUnlimitedWithAllString() {
    inout.getInputQueue().add("all");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 0, 1, 2 }));
  }

  @Test
  public void shouldSkipUnlimited() {
    inout.getInputQueue().add("skip");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void shouldSkipUnlimitedWithEmptyString() {
    inout.getInputQueue().add("  ");
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

    assertThat(inout.getOutputBuffer().size(), is(0));
  }
}
