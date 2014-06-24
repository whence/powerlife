using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class CountingHouse : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            var coppers = context.ActivePlayer.DiscardArea.OfType<Copper>();
            if (coppers.Any())
            {
                coppers.MoveAll(context.ActivePlayer.DiscardArea, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context);
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " put all coppers from their discard pile into their hand");
            }
            else
            {
                context.Game.Log.LogMessage("Counting house did nothing - no Copper in discard pile.");
            }
        }
        #endregion
    }
}
