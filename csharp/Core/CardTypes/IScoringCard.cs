using System.Collections.Generic;

namespace Powercards.Core
{
    public interface IScoringCard : ICard
    {
        #region methods
        int Score(IEnumerable<ICard> allCards);
        #endregion
    }
}
