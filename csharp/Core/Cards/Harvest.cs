using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 5, true)]
    public class Harvest : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(4, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                context.AvailableSpend += transZone.DistinctByName().Count();

                transZone.MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            }
        }
        #endregion
    }
}
