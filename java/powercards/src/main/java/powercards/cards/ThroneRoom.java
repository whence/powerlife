package powercards.cards;

import powercards.ActionCard;
import powercards.Card;
import powercards.Cards;
import powercards.Choices;
import powercards.Game;

import java.util.OptionalInt;

public class ThroneRoom extends Card implements ActionCard {
  @Override
  public int getCost(Game game) {
    return 4;
  }

  @Override
  public String toString() {
    return "Throne Room";
  }

  @Override
  public void play(Game game) {
    OptionalInt iAction = game.getDialog().chooseMandatoryOne("Select an action card to play twice",
        Choices.ofCards(game.getActivePlayer().getHand(), c -> c instanceof ActionCard));
    if (iAction.isPresent()) {
      Card actionCard = Cards.moveOne(game.getActivePlayer().getHand(), game.getActivePlayer().getPlayed(),
          iAction.getAsInt());
      game.getDialog().inout().output("Playing " + actionCard + " first time");
      ((ActionCard) actionCard).play(game);
      game.getDialog().inout().output("Playing " + actionCard + " second time");
      ((ActionCard) actionCard).play(game);
    } else {
      game.getDialog().inout().output("No action card available to play");
    }
  }
}
