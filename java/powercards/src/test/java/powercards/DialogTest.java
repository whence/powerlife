package powercards;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    assertEquals(OptionalInt.of(2), dialog.chooseOne("blah", Arrays.asList(
        new Choice("0", false), new Choice("1", true), new Choice("2", true))));
  }

  @Test
  public void shouldChooseOptionalOne() {
    inout.queueInputs("1");
    assertEquals(OptionalInt.of(1), dialog.chooseOne("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", false))));
  }

  @Test
  public void shouldSkipOptionalOne() {
    inout.queueInputs("skip");
    assertFalse(dialog.chooseOptionalOne("blah", Arrays.asList(
            new Choice("0", false), new Choice("1", false), new Choice("2", true))
    ).isPresent());
  }

  @Test
  public void shouldChooseUnlimited() {
    inout.queueInputs("1, 2");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertTrue(result.isPresent());
    assertArrayEquals(new int[]{ 1, 2 }, result.get());
  }

  @Test
  public void shouldChooseUnlimitedWithAll() {
    inout.queueInputs("1,2, 0,");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertTrue(result.isPresent());
    assertArrayEquals(new int[]{1, 2, 0}, result.get());
  }

  @Test
  public void shouldChooseUnlimitedWithAllString() {
    inout.queueInputs("all");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertTrue(result.isPresent());
    assertArrayEquals(new int[]{ 0, 1, 2 }, result.get());
  }

  @Test
  public void shouldSkipUnlimited() {
    inout.queueInputs("skip");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertFalse(result.isPresent());
  }

  @Test
  public void shouldSkipUnlimitedWithEmptyString() {
    inout.queueInputs("  ");
    Optional<int[]> result = dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", true), new Choice("1", true), new Choice("2", true)));
    assertFalse(result.isPresent());
  }

  @Test
  public void shouldReturnNotPresentWhenUnableToChoose() {
    assertFalse(dialog.chooseOne("", new ArrayList<>()).isPresent());
    assertFalse(dialog.chooseOne("blah", Arrays.asList(
        new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent());

    assertFalse(dialog.chooseOptionalOne("", new ArrayList<>()).isPresent());
    assertFalse(dialog.chooseOptionalOne("blah", Arrays.asList(
        new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent());

    assertFalse(dialog.chooseUnlimited("", new ArrayList<>()).isPresent());
    assertFalse(dialog.chooseUnlimited("blah", Arrays.asList(
        new Choice("0", false), new Choice("1", false), new Choice("2", false))
    ).isPresent());

    assertTrue(inout.noOutput());
  }
}
