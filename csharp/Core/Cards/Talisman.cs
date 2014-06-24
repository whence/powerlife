using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 4, true)]
    public class Talisman : CardBase, ITreasureCard, IPostBuyOtherCardEventCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 1);
        }

        public void AfterBuy(TurnContext context, Player player, ICard card)
        {
            if (new CardCostValidator(context, player, 0, 4)
                .And(new InvertValidator<ICard>(new CardTypeValidator<IVictoryCard>()))
                .Validate(card))
            {
                var pile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new NameValidator<ICard>(card.Name)).Validate);
                if (pile != null)
                {
                    if (pile.TopCard.MoveTo(pile, player.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(player, pile);
                }
            }
        }
        #endregion
    }
}
