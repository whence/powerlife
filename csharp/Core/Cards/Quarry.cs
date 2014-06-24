using System;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 4, true)]
    public class Quarry : CardBase, ITreasureCard, IInPlayCostModifierCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 1);
        }

        public int OnEvalCardCost(ICard card, int cost)
        {
            return new CardTypeValidator<IActionCard>().Validate(card) ? Math.Max(cost - 2, 0) : cost;
        }
        #endregion
    }
}
