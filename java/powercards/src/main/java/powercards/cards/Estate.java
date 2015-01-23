package powercards.cards;

import powercards.Card;
import powercards.VictoryCard;

public class Estate extends Card implements VictoryCard {
  @Override
  public int getVictoryPoint() {
    return 1;
  }
}
