using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 7, true)]
    public class Expand : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to Expand.").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                var maxCostOfCardToGain = trashCard.GetCost(context, context.ActivePlayer) + 3;
                var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, 0, maxCostOfCardToGain)).Validate).ToArray();
                if (pilesToGain.Length > 0)
                {
                    var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain,
                        new CountValidator<CardSupplyPile>(1), 
                        string.Format("Select a card to gain of cost {0} or less.", maxCostOfCardToGain)).Single();

                    if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(context.ActivePlayer, gainPile);
                }
                else
                {
                    context.Game.Log.LogMessage("{0} could gain no card of appropriate cost", context.ActivePlayer);
                }
            }
            else
            {
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " did not have any cards to Expand.");
            }
        }
        #endregion
    }
}