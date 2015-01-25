package powercards;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class ChoicesTest {
  @Test
  public void shouldConvertToChoices() {
    List<Choice> choices = Choices.of(Arrays.asList(new Copper(), new Estate()), c -> c instanceof VictoryCard);
    assertThat(choices.size(), is(2));
    assertThat(choices.get(0).getName(), is("Copper"));
    assertThat(choices.get(0).isSelectable(), is(false));
    assertThat(choices.get(1).getName(), is("Estate"));
    assertThat(choices.get(1).isSelectable(), is(true));
  }
}
