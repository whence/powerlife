package powercards;

public class Pile {
  public Pile(Class<? extends Card> clazz, int size) throws IllegalAccessException, InstantiationException {
    Card sample = clazz.newInstance();
  }
}
