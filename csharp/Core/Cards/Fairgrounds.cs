using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 6, true)]
    public class Fairgrounds : CardBase, IVictoryCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return Toolbox.RoundDown(allCards.DistinctByName().Count(), 5) * 2;
        }
        #endregion
    }
}
