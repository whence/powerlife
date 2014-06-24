using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class GreatHall : CardBase, IActionCard, IVictoryCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
        }

        public int Score(IEnumerable<ICard> allCards)
        {
            return 1;
        }
        #endregion
    }
}
