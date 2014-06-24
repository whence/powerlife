using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 3, true)]
    public class Lookout : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(3, transZone, CardMovementVerb.Lookat, context);

                if (!transZone.IsEmpty)
                {
                    var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, transZone,
                        new CountValidator<ICard>(1), "Select a card to trash").Single();

                    if (trashCard.MoveTo(transZone, context.Game.TrashZone, CardMovementVerb.Trash, context))
                        context.Game.Log.LogTrash(context.ActivePlayer, trashCard);
                }

                if (!transZone.IsEmpty)
                {
                    var discardCard = context.Game.Dialog.Select(context, context.ActivePlayer, transZone,
                        new CountValidator<ICard>(1), "Select a card to discard").Single();

                    if (discardCard.MoveTo(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context))
                        context.Game.Log.LogDiscard(context.ActivePlayer, discardCard);
                }

                if (!transZone.IsEmpty)
                {
                    var putBackCard = context.Game.Dialog.Select(context, context.ActivePlayer, transZone,
                        new CountValidator<ICard>(1), "Select a card to put back on top deck").Single();

                    if (putBackCard.MoveTo(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context))
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " put back a card to top deck");
                }
            }
        }
        #endregion
    }
}
