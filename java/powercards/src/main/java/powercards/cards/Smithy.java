package powercards.cards;

import powercards.ActionCard;
import powercards.Card;
import powercards.Cards;
import powercards.Game;

public class Smithy extends Card implements ActionCard {
  @Override
  public int getCost(Game game) {
    return 4;
  }

  @Override
  public void play(Game game) {
    Cards.drawCards(game.getActivePlayer(), 3, game.getDialog().inout());
  }
}
