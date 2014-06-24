using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 6, true)]
    public class Hoard : CardBase, ITreasureCard, IPostBuyOtherCardEventCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 2);
        }

        public void AfterBuy(TurnContext context, Player player, ICard card)
        {
            if (new CardTypeValidator<IVictoryCard>().Validate(card))
            {
                var goldPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Gold>()).Validate);
                if (goldPile != null)
                {
                    if (goldPile.TopCard.MoveTo(goldPile, player.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(player, goldPile);
                }
            }
        }
        #endregion
    }
}
