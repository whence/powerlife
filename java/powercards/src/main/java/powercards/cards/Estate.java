package powercards.cards;

import powercards.Card;
import powercards.Game;
import powercards.VictoryCard;

public class Estate extends Card implements VictoryCard {
  @Override
  public int getVictoryPoint() {
    return 1;
  }

  @Override
  public int getCost(Game game) {
    return 2;
  }
}
