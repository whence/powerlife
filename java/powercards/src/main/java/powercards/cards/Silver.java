package powercards.cards;

import powercards.Card;
import powercards.Game;
import powercards.TreasureCard;

public class Silver extends Card implements TreasureCard {
  @Override
  public void play(Game game) {
    game.getActivePlayer().addCoins(2);
  }

  @Override
  public int getCost(Game game) {
    return 3;
  }
}
