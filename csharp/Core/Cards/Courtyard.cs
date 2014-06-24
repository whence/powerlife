using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 2, true)]
    public class Courtyard : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(3, context);

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var putBackCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to put on top of the deck.").Single();

                if (putBackCard.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context))
                    context.Game.Log.LogPutBack(context.ActivePlayer, putBackCard);
            }
        }
        #endregion
    }
}