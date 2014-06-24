using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class Duke : CardBase, IVictoryCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return allCards.OfType<Duchy>().Count();
        }
        #endregion
    }
}