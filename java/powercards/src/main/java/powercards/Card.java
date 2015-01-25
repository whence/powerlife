package powercards;

public abstract class Card {
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public String toString() {
    return this.getName();
  }

  public abstract int getCost(Game game);
}
