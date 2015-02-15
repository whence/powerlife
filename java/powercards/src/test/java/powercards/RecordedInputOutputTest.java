package powercards;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecordedInputOutputTest {
  private RecordedInputOutput inout;

  @Before
  public void setUp() {
    inout = new RecordedInputOutput();
  }

  @Test
  public void shouldQueueInputs() {
    inout.queueInputs("1", "2", "3");

    assertEquals("1", inout.input());
    assertEquals("2", inout.input());
    assertEquals("3", inout.input());
    assertEquals(0, inout.getInputQueue().size());
  }

  @Test
  public void shouldHasOutputsWhenNoGap() {
    inout.output("wes");
    inout.output("bec");

    assertTrue(inout.hasOutputs("wes", "bec"));
    assertFalse(inout.hasOutputs("bec", "wes"));
    assertFalse(inout.hasOutputs("wes", "Bec"));

    assertEquals(2, inout.getOutputBuffer().size());
  }

  @Test
  public void shouldHasOutputsWhenHavingGap() {
    inout.output("x");
    inout.output("WES".toLowerCase());
    inout.output("y");
    inout.output("bec");
    inout.output("z");

    assertTrue(inout.hasOutputs("wes", "bec"));
    assertFalse(inout.hasOutputs("bec", "wes"));
    assertFalse(inout.hasOutputs("wes", "bec", "y"));

    assertEquals(5, inout.getOutputBuffer().size());
  }

  @Test
  public void shouldHasOutputsWhenRepeat() {
    inout.output("wes");
    inout.output("bec");
    inout.output("x");
    inout.output("wes");
    inout.output("bec");
    inout.output("y");

    assertTrue(inout.hasOutputs("wes", "bec"));
    assertTrue(inout.hasOutputs("wes", "bec", "wes"));
    assertTrue(inout.hasOutputs("wes", "bec", "bec"));
    assertTrue(inout.hasOutputs("wes", "bec", "wes", "bec"));
    assertTrue(inout.hasOutputs("wes", "bec", "y"));
    assertTrue(inout.hasOutputs("x", "bec"));
    assertFalse(inout.hasOutputs("wes", "bec", "bec", "wes"));
    assertFalse(inout.hasOutputs("bec", "x", "x"));

    assertEquals(6, inout.getOutputBuffer().size());
  }
}
