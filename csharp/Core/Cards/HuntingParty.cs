using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 5, true)]
    public class HuntingParty : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;

            context.Game.Log.LogRevealHand(context.ActivePlayer);

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                var cardValidator = new InvertValidator<ICard>(new NamesValidator<ICard>(context.ActivePlayer.Hand.Select(x => x.Name)));
                context.ActivePlayer.MoveFromTopDeckTill(cardValidator, 1, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                var drawCard = transZone.SingleOrDefault(cardValidator.Validate);
                if (drawCard != null)
                {
                    if (drawCard.MoveTo(transZone, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context))
                        context.Game.Log.LogMessage("{0} put {1} in hand", context.ActivePlayer.Name, drawCard.Name);
                }
                transZone.MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            }
        }
        #endregion
    }
}
