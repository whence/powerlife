using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 8, false)]
    public class Province : CardBase, IVictoryCard, IGameEndingCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return 6;
        }
        #endregion
    }
}