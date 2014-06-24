using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Promo, 4, true)]
    public class Envoy : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(5, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                if (!transZone.IsEmpty)
                {
                    var discardCard = context.Game.Dialog.Select(context, context.Game.NextPlayerOf(context.ActivePlayer), transZone,
                        new CountValidator<ICard>(1),
                        string.Format("Select the card you do not want {0} to draw.", transZone.Owner.Name)).Single();

                    if (discardCard.MoveTo(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context))
                        context.Game.Log.LogDiscard(context.ActivePlayer, discardCard);

                    transZone.MoveAll(transZone, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context);
                }
            }
        }
        #endregion
    }
}