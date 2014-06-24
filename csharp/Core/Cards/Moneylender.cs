using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Moneylender : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            var copper = context.ActivePlayer.Hand.OfType<Copper>().FirstOrDefault();
            if (copper != null)
            {
                if (copper.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, copper);
                
                context.AvailableSpend += 3;
            }
        }
        #endregion
    }
}
