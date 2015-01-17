package powercards;

import java.util.Arrays;
import java.util.List;
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
    int[] invSourceIndexes = inverseIndexes(sourceIndexes, source.size());
    List<Card> invCards = Arrays.stream(invSourceIndexes).mapToObj(source::get).collect(Collectors.toList());
    source.clear();
    source.addAll(invCards);
    target.addAll(cards);
    return cards;
  }

  /*
   * Return the inverse of the provided indexes.
   * For example, [1, 2, 4] with cap of 6 will return [0, 3, 5].
   * Provided indexes must be sorted.
   */
  public static int[] inverseIndexes(int[] indexes, int capExclusive) {
    return IntStream.range(0, capExclusive).filter(i -> Arrays.binarySearch(indexes, i) < 0).toArray();
  }
}
