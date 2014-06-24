using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 4, true)]
    public class FarmingVillage : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 2;

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                var cardValidator = new CardTypeValidator<IActionCard, ITreasureCard>();
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
