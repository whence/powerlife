package powercards;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  public static Card moveOne(Pile pile, List<Card> target) {
    Card card = pile.pop();
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

  public static <T extends Card> T of(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(String.format(
          "Cannot create %s (possibly no default constructor?)", clazz.getSimpleName()));
    }
  }

  public static List<Card> drawCards(Player player, int n) {
    if (player.getDeck().size() > n) {
      return drawCardsNoRecycle(player, n);
    }

    List<Card> cards = drawCardsFullDeck(player);

    while (cards.size() < n && !player.getDiscard().isEmpty()) {
      player.getDeck().addAll(player.getDiscard());
      player.getDiscard().clear();
      Collections.shuffle(player.getDeck());

      int remaining = n - cards.size();
      if (player.getDeck().size() > remaining) {
        cards.addAll(drawCardsNoRecycle(player, remaining));
        return cards;
      }

      cards.addAll(drawCardsFullDeck(player));
    }

    return cards;
  }

  private static List<Card> drawCardsNoRecycle(Player player, int n) {
    List<Card> sub = player.getDeck().subList(player.getDeck().size() - n, player.getDeck().size());

    List<Card> cards = new ArrayList<>(sub);
    Collections.reverse(cards);

    sub.clear();
    player.getHand().addAll(cards);

    return cards;
  }

  private static List<Card> drawCardsFullDeck(Player player) {
    List<Card> cards = new ArrayList<>(player.getDeck());
    Collections.reverse(cards);

    player.getDeck().clear();
    player.getHand().addAll(cards);

    return cards;
  }
}
