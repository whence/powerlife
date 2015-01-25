package powercards;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Choices {
  public static List<Choice> of(List<Card> cards, Predicate<Card> predicate) {
    return cards.stream().map(c -> new Choice(c.getName(), predicate.test(c))).collect(Collectors.toList());
  }
}
