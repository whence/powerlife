using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, false)]
    public class Duchy : CardBase, IVictoryCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return 3;
        }
        #endregion
    }
}