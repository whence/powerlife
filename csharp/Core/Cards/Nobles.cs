using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 6, true)]
    public class Nobles : CardBase, IVictoryCard, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            switch (context.Game.Dialog.Choose(context, context.ActivePlayer, new[] { "Draw 3 cards", "Gain 2 actions", }, 1, "Choose one")[0])
            {
                case 0:
                    context.ActivePlayer.DrawCards(3, context);
                    break;

                case 1:
                    context.RemainingActions += 2;
                    break;
            }
        }

        public int Score(IEnumerable<ICard> allCards)
        {
            return 2;
        }
        #endregion
    }
}
