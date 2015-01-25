package powercards.cards;

import powercards.Card;
import powercards.Game;
import powercards.VictoryCard;

public class Duchy extends Card implements VictoryCard {
  @Override
  public int getVictoryPoint() {
    return 3;
  }

  @Override
  public int getCost(Game game) {
    return 5;
  }
}