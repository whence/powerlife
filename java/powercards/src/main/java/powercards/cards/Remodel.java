package powercards.cards;

import powercards.*;

import java.util.OptionalInt;

public class Remodel extends Card implements ActionCard {
  @Override
  public int getCost(Game game) {
    return 4;
  }

  @Override
  public void play(Game game) {
    OptionalInt iTrash = game.getDialog().chooseOne("Select a card to trash",
        Choices.ofCards(game.getActivePlayer().getHand(), c -> true));
    if (iTrash.isPresent()) {
      Card trashedCard = Cards.moveOne(game.getActivePlayer().getHand(), game.getBoard().getTrash(),
          iTrash.getAsInt());
      game.getInputOutput().output("Trashed " + trashedCard);

      OptionalInt iGain = game.getDialog().chooseOne("Select a pile to gain",
          Choices.ofPiles(game.getBoard().getPiles(),
              p -> !p.isEmpty() && p.getSample().getCost(game) <= trashedCard.getCost(game) + 2));
      if (iGain.isPresent()) {
        Card gainedCard = Cards.moveOne(game.getBoard().getPiles().get(iGain.getAsInt()),
            game.getActivePlayer().getDiscard());
        game.getInputOutput().output("Gained " + gainedCard);
      } else {
        game.getInputOutput().output("No pile available to gain");
      }
    } else {
      game.getInputOutput().output("No card in hard to trash");
    }
  }
}
