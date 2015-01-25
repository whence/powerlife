package powercards;

public class DummyActionCard extends Card implements ActionCard {
  @Override
  public void play(Game game) {
  }

  @Override
  public int getCost(Game game) {
    return 0;
  }
}
