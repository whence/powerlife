using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Mine : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (context.ActivePlayer.Hand.OfType<ITreasureCard>().Any())
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1).AndEach(new CardTypeValidator<ITreasureCard>()),
                    "Select a treasure card to mine").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                var maxCostOfCardToGain = trashCard.GetCost(context, context.ActivePlayer) + 3;
                var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, 0, maxCostOfCardToGain).And(new CardTypeValidator<ITreasureCard>())).Validate).ToArray();
                if (pilesToGain.Length > 0)
                {
                    var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain, 
                        new CountValidator<CardSupplyPile>(1), 
                        string.Format("Select a treasure card to gain of cost up to ${0}", maxCostOfCardToGain)).Single();

                    if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.Hand, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(context.ActivePlayer, gainPile);
                }
                else
                {
                    context.Game.Log.LogMessage("{0} could gain no treasure card of appropriate cost", context.ActivePlayer);
                }
            }
            else
            {
                context.Game.Log.LogMessage("No treasure cards to trash.");
            }
        }
        #endregion
    }
}
