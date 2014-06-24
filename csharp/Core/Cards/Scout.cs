using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class Scout : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(4, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                transZone.OfType<IVictoryCard>().MoveAll(transZone, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context);
                
                if (!transZone.IsEmpty)
                {
                    var putBackCard = context.Game.Dialog.Select(context, context.ActivePlayer, transZone,
                        new CountValidator<ICard>(transZone.CardCount),
                        "Select these cards to be put back in order, last on top");

                    putBackCard.MoveAll(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context);
                    context.Game.Log.LogMessage("{0} put back {1} to deck", context.ActivePlayer.Name, putBackCard.Length);
                }
            }
        }
        #endregion
    }
}
