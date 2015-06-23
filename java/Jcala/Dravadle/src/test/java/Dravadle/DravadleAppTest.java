package Dravadle;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class DravadleAppTest {
  @Test
  public void shouldFail() {
    assertThat("wes", is("Wes"));
  }
}