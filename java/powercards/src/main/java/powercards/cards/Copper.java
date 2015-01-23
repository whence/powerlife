package powercards.cards;

import powercards.Card;
import powercards.Game;
import powercards.TreasureCard;

public class Copper extends Card implements TreasureCard {
  @Override
  public void play(Game game) {
    game.getActivePlayer().addCoins(1);
  }
}
