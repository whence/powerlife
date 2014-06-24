using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 2, false)]
    public class Estate : CardBase, IVictoryCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return 1;
        }
        #endregion
    }
}