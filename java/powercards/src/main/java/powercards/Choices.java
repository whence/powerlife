package powercards;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Choices {
  public static List<Choice> ofCards(List<Card> cards, Predicate<Card> predicate) {
    return cards.stream().map(c -> new Choice(c.getName(), predicate.test(c)))
        .collect(Collectors.toList());
  }

  public static List<Choice> ofPiles(List<Pile> piles, Predicate<Pile> predicate) {
    return piles.stream().map(p -> new Choice(p.getSample().getName(), predicate.test(p)))
        .collect(Collectors.toList());
  }
}
