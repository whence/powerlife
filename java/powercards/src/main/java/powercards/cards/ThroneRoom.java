package powercards.cards;

import powercards.ActionCard;
import powercards.Card;
import powercards.Game;

public class ThroneRoom extends Card implements ActionCard {
  @Override
  public int getCost(Game game) {
    return 4;
  }

  @Override
  public String getName() {
    return "Throne Room";
  }

  @Override
  public void play(Game game) {
  }
}
