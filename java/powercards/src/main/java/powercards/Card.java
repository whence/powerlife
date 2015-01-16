package powercards;

public abstract class Card {
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
