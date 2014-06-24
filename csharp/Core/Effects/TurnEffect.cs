using System;

namespace Powercards.Core
{
    public class TurnEffect
    {
        #region properties
        public Func<ITreasureCard, TurnContext, int, int> OnEvalTreasureValue { get; set; }
        public Func<ICard, int, int> OnEvalCardCost { get; set; }
        #endregion
    }
}
