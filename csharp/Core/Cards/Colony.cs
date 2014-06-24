using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 11, false)]
    public class Colony : CardBase, IVictoryCard, IGameEndingCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return 10;
        }
        #endregion
    }
}