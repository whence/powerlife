using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 7, true)]
    public class Forge : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            var costOfCardToGain = 0;
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new NullValidator<IEnumerable<ICard>>(), "Choose any number of cards to trash from hand");

                trashCards.MoveAll(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context);
                foreach (var card in trashCards)
                {
                    costOfCardToGain += card.GetCost(context, context.ActivePlayer);
                    context.Game.Log.LogTrash(context.ActivePlayer, card);
                }
            }

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
        #endregion
    }
}