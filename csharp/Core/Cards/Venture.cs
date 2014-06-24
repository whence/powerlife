using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Venture : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 1);

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeckTill(new CardTypeValidator<ITreasureCard>(), 1, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                transZone.Where(new InvertValidator<ICard>(new CardTypeValidator<ITreasureCard>()).Validate)
                    .MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
                
                var treasureCard = (ITreasureCard)transZone.SingleOrDefault();
                if (treasureCard != null)
                {
                    Enforce.IsTrue(treasureCard.MoveTo(transZone, context.ActivePlayer.PlayArea, CardMovementVerb.Play, context));
                    treasureCard.PlayAndProduceValue(context);
                }
            }
        }
        #endregion
    }
}
