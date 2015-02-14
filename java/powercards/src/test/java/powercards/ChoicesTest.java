package powercards;

import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ChoicesTest {
  @Test
  public void shouldConvertCardsToChoices() {
    List<Card> cards = Arrays.asList(new Copper(), new Estate());
    List<Choice> choices = Choices.ofCards(cards, c -> c instanceof VictoryCard);
    assertThat(choices.size(), is(2));
    assertThat(choices.get(0).getName(), is("Copper"));
    assertThat(choices.get(0).isSelectable(), is(false));
    assertThat(choices.get(1).getName(), is("Estate"));
    assertThat(choices.get(1).isSelectable(), is(true));
  }

  @Test
  public void shouldConvertPilesToChoices() {
    List<Pile> piles = Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8));
    List<Choice> choices = Choices.ofPiles(piles, p -> p.getSample() instanceof TreasureCard);
    assertThat(choices.size(), is(2));
    assertThat(choices.get(0).getName(), is("Copper"));
    assertThat(choices.get(0).isSelectable(), is(true));
    assertThat(choices.get(1).getName(), is("Estate"));
    assertThat(choices.get(1).isSelectable(), is(false));
  }
}
