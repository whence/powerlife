package powercards;

import java.util.Stack;

public class Pile {
  private final Card sample;
  private int size;
  private final Class<? extends Card> factory;
  private final Stack<Card> buffer;

  public Card getSample() {
    return sample;
  }

  public Pile(Class<? extends Card> factory, int size) {
    this.factory = factory;
    this.sample = createCard(factory);
    this.size = size;
    this.buffer = new Stack<>();
  }

  public boolean isEmpty() {
    return size <= 0;
  }

  public int size() {
    return size;
  }

  public void push(Card card) {
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
      card = createCard(factory);
    }
    size -= 1;
    return card;
  }

  private static Card createCard(Class<? extends Card> factory) {
    try {
      return factory.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(String.format(
          "Cannot create %s (possibly no default constructor?)", factory.getSimpleName()));
    }
  }
}
