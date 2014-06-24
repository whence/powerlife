using System;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class WishingWell : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;

            var wishCardName = context.Game.Dialog.Name(context, context.ActivePlayer, "Name a card");
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(1, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                if (!transZone.IsEmpty)
                {
                    var drawCard = transZone.Single();
                    if (drawCard.Name.Equals(wishCardName, StringComparison.Ordinal))
                    {
                        if (drawCard.MoveTo(transZone, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context))
                            context.Game.Log.LogMessage(context.ActivePlayer.Name + " named the correct card and put it in hand");
                    }
                    else
                    {
                        if (drawCard.MoveTo(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context))
                            context.Game.Log.LogMessage(context.ActivePlayer.Name + " named the wrong card and put it back to top deck");
                    }
                }
                else
                {
                    context.Game.Log.LogMessage("There is no card in the deck to draw");
                }
            }
        }
        #endregion
    }
}
