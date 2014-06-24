using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 4, true)]
    public class Remake : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            for (int i = 0; i < 2; i++)
            {
                if (!context.ActivePlayer.Hand.IsEmpty)
                {
                    var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                        new CountValidator<ICard>(1), "Select a card to Remake").Single();

                    if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                        context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                    var costOfCardToGain = trashCard.GetCost(context, context.ActivePlayer) + 1;
                    var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, costOfCardToGain)).Validate).ToArray();
                    if (pilesToGain.Length > 0)
                    {
                        var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain,
                            new CountValidator<CardSupplyPile>(1),
                            string.Format("Select a card to gain of exact cost of {0}", costOfCardToGain)).Single();

                        if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(context.ActivePlayer, gainPile);
                    }
                    else
                    {
                        context.Game.Log.LogMessage("{0} could gain no card of appropriate cost", context.ActivePlayer);
                    }
                }
            }
        }
        #endregion
    }
}
