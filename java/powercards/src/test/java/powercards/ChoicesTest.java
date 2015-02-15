package powercards;

import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChoicesTest {
  @Test
  public void shouldConvertCardsToChoices() {
    List<Card> cards = Arrays.asList(new Copper(), new Estate());
    List<Choice> choices = Choices.ofCards(cards, c -> c instanceof VictoryCard);

    assertEquals(2, choices.size());
    assertEquals("Copper", choices.get(0).getName());
    assertFalse(choices.get(0).isSelectable());
    assertEquals("Estate", choices.get(1).getName());
    assertTrue(choices.get(1).isSelectable());
  }

  @Test
  public void shouldConvertPilesToChoices() {
    List<Pile> piles = Arrays.asList(new Pile(Copper::new, 10), new Pile(Estate::new, 8));
    List<Choice> choices = Choices.ofPiles(piles, p -> p.getSample() instanceof TreasureCard);

    assertEquals(2, choices.size());
    assertEquals("Copper", choices.get(0).getName());
    assertTrue(choices.get(0).isSelectable());
    assertEquals("Estate", choices.get(1).getName());
    assertFalse(choices.get(1).isSelectable());
  }
}
