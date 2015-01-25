package powercards.cards;

import powercards.Card;
import powercards.Game;
import powercards.VictoryCard;

public class Province extends Card implements VictoryCard {
  @Override
  public int getVictoryPoint() {
    return 6;
  }

  @Override
  public int getCost(Game game) {
    return 8;
  }
}
