package powercards;

public abstract class Card {
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public abstract int getCost(Game game);
}
