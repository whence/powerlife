using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 5, true)]
    public class HornOfPlenty : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            var maxCostOfCardToGain = context.ActivePlayer.PlayArea.DistinctByName().Count();
            var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, 0, maxCostOfCardToGain)).Validate).ToArray();
            if (pilesToGain.Length > 0)
            {
                var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain,
                    new CountValidator<CardSupplyPile>(1),
                    string.Format("Select a card to gain of cost {0} or less.", maxCostOfCardToGain)).Single();

                var gainCard = gainPile.TopCard;
                if (gainCard.MoveTo(gainPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                    context.Game.Log.LogGain(context.ActivePlayer, gainPile);

                if (new CardTypeValidator<IVictoryCard>().Validate(gainCard))
                {
                    if (this.CurrentZone != context.Game.TrashZone)
                    {
                        if (this.MoveTo(context.ActivePlayer.PlayArea, context.Game.TrashZone, CardMovementVerb.Trash, context))
                            context.Game.Log.LogTrash(context.ActivePlayer, this);
                    }
                }
            }
            else
            {
                context.Game.Log.LogMessage("{0} could gain no card of appropriate cost", context.ActivePlayer);
            }
        }
        #endregion
    }
}
