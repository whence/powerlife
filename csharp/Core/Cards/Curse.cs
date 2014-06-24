using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 0, false)]
    public class Curse : CardBase, IScoringCard
    {
        #region methods
        public int Score(IEnumerable<ICard> allCards)
        {
            return -1;
        }
        #endregion
    }
}