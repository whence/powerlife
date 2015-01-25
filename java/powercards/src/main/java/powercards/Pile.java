package powercards;

import java.util.Stack;

public class Pile {
  private final Card sample;
  private int size;
  private final Class<? extends Card> clazz;
  private final Stack<Card> buffer;

  public Pile(Class<? extends Card> clazz, int size) {
    this.clazz = clazz;
    this.sample = Cards.of(clazz);
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
      card = Cards.of(clazz);
    }
    size -= 1;
    return card;
  }
}
