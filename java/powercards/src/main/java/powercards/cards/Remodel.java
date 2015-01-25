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
    OptionalInt idxTrash = game.getDialog().chooseOne("Select a card to trash",
        Choices.ofCards(game.getActivePlayer().getHand(), c -> true));
    if (idxTrash.isPresent()) {
      Card trashedCard = Cards.moveOne(game.getActivePlayer().getHand(), game.getBoard().getTrash(),
          idxTrash.getAsInt());
      game.getInputOutput().output("Trashed " + trashedCard);

      OptionalInt idxGain = game.getDialog().chooseOne("Select a pile to gain",
          Choices.ofPiles(game.getBoard().getPiles(),
              p -> !p.isEmpty() && p.getSample().getCost(game) <= trashedCard.getCost(game) + 2));
      if (idxGain.isPresent()) {
        Card gainedCard = Cards.moveOne(game.getBoard().getPiles().get(idxGain.getAsInt()),
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
