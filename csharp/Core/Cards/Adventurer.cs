using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 6, true)]
    public class Adventurer : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeckTill(new CardTypeValidator<ITreasureCard>(), 2, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                transZone.OfType<ITreasureCard>().MoveAll(transZone, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context);
                transZone.MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            }
        }
        #endregion
    }
}
