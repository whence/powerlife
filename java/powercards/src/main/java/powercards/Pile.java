package powercards;

import java.util.Stack;
import java.util.function.Supplier;

public class Pile {
  private final Card sample;
  private int size;
  private final Supplier<Card> cardSupplier;
  private final Stack<Card> buffer;

  public Pile(Supplier<Card> cardSupplier, int size) {
    this.cardSupplier = cardSupplier;
    this.sample = cardSupplier.get();
    this.size = size;
    this.buffer = new Stack<>();
  }

  public Card getSample() {
    return sample;
  }

  public boolean isEmpty() {
    return size <= 0;
  }

  public int size() {
    return size;
  }

  public void push(Card card) {
    if (!card.getName().equals(sample.getName())) {
      throw new IllegalArgumentException(String.format("Cannot push %s card into %s pile",
          card.getName(), sample.getName()));
    }
    buffer.push(card);
    size += 1;
  }

  public Card pop() {
    if (isEmpty()) {
      throw new IllegalStateException("Cannot pop when the pile is empty");
    }
    Card card;
    if (!buffer.isEmpty()) {
      card = buffer.pop();
    } else {
      card = cardSupplier.get();
    }
    size -= 1;
    return card;
  }
}
