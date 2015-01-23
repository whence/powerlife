package powercards;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Cards {
  public static Card moveOne(List<Card> source, List<Card> target, int sourceIndex) {
    Card card = source.get(sourceIndex);
    source.remove(sourceIndex);
    target.add(card);
    return card;
  }

  public static List<Card> moveMany(List<Card> source, List<Card> target, int[] sourceIndexes) {
    List<Card> cards = Arrays.stream(sourceIndexes).mapToObj(source::get).collect(Collectors.toList());

    if (sourceIndexes.length > source.size() / 2) {
      int[] invSourceIndexes = inverseIndexes(sortIndexes(sourceIndexes, false), source.size());
      List<Card> invCards = Arrays.stream(invSourceIndexes).mapToObj(source::get).collect(Collectors.toList());
      source.clear();
      source.addAll(invCards);
    } else {
      for (int i : sortIndexes(sourceIndexes, true)) {
        source.remove(i);
      }
    }
    target.addAll(cards);
    return cards;
  }

  /*
   * Return the inverse of the provided indexes.
   * For example, [1, 2, 4] with cap of 6 will return [0, 3, 5].
   * Provided indexes must be sorted ascending.
   */
  public static int[] inverseIndexes(int[] indexes, int capExclusive) {
    return IntStream.range(0, capExclusive).filter(i -> Arrays.binarySearch(indexes, i) < 0).toArray();
  }

  public static int[] sortIndexes(int[] original, boolean descending) {
    int[] array = original.clone();
    Arrays.sort(array);
    if (descending) {
      ArrayUtils.reverse(array);
    }
    return array;
  }

  public static List<Choice> toChoices(List<Card> cards, Predicate<Card> predicate) {
    return cards.stream().map(c -> new Choice(c.getName(), predicate.test(c))).collect(Collectors.toList());
  }
}
