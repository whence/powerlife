package powercards;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    inout.queueInputs("2");
    assertThat(dialog.chooseOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", true), new Choice("2", true))
    ), is(OptionalInt.of(2)));
  }

  @Test
  public void shouldChooseOptionalOne() {
    inout.queueInputs("1");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", false))
    ), is(OptionalInt.of(1)));
  }

  @Test
  public void shouldSkipOptionalOne() {
    inout.queueInputs("skip");
    assertThat(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", true))
    ).isPresent(), is(false));
  }

  @Test
  public void shouldChooseUnlimited() {
    inout.queueInputs("1, 2");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2 }));
  }

  @Test
  public void shouldChooseUnlimitedWithAll() {
    inout.queueInputs("1,2, 0,");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 1, 2, 0 }));
  }

  @Test
  public void shouldChooseUnlimitedWithAllString() {
    inout.queueInputs("all");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(new int[] { 0, 1, 2 }));
  }

  @Test
  public void shouldSkipUnlimited() {
    inout.queueInputs("skip");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
            new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void shouldSkipUnlimitedWithEmptyString() {
    inout.queueInputs("  ");
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

    assertTrue(inout.noOutput());
  }
}
