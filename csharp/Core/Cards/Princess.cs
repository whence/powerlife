using System;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 0, false)]
    public class Princess : CardBase, IActionCard, IInPlayCostModifierCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;
        }

        public int OnEvalCardCost(ICard card, int cost)
        {
            return Math.Max(cost - 2, 0);
        }
        #endregion
    }
}
