using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 6, true)]
    public class Harem : CardBase, ITreasureCard, IVictoryCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 2);
        }

        public int Score(IEnumerable<ICard> allCards)
        {
            return 2;
        }
        #endregion
    }
}
