using System;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class Bridge : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;
            context.AvailableSpend += 1;

            context.AddTurnEffect(new TurnEffect { OnEvalCardCost = OnEvalCardCost, });
        }

        private static int OnEvalCardCost(ICard card, int cost)
        {
            return Math.Max(cost - 1, 0);
        }
        #endregion
    }
}
