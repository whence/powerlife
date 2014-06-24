using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 2, true)]
    public class Embargo : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;
            
            if (this.CurrentZone != context.Game.TrashZone)
            {
                if (this.MoveTo(context.ActivePlayer.PlayArea, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, this);
            }

            var embargoPile = context.Game.Dialog.Select(context, context.ActivePlayer, context.Game.Supply.AllPiles, 
                new CountValidator<CardSupplyPile>(1), "Select a pile to put an embargo token on").Single();

            embargoPile.Embargo();
        }
        #endregion
    }
}
