package Xcaladle;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class XcalaServiceTest {
  @Test
  public void shouldFail() {
    assertThat(new XcalaService().hello(), is("hello from xcala"));
  }
}