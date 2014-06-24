using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Gardens : CardBase, IVictoryCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return Toolbox.RoundDown(allCards.Count(), 10);
        }
        #endregion
    }
}
