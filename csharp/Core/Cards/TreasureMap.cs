using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class TreasureMap : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            var mapCountTrashed = 0;
            
            if (this.CurrentZone != context.Game.TrashZone)
            {
                if (this.MoveTo(context.ActivePlayer.PlayArea, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, this);

                mapCountTrashed++;
            }

            var otherMap = context.ActivePlayer.Hand.OfType<TreasureMap>().FirstOrDefault();
            if (otherMap != null)
            {
                if (otherMap.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, otherMap);

                mapCountTrashed++;
            }

            if (mapCountTrashed == 2)
            {
                var goldPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Gold>()).Validate);
                for (int i = 0; i < 4; i++)
                {
                    if (goldPile != null && !goldPile.IsEmpty)
                    {
                        if (goldPile.TopCard.MoveTo(goldPile, context.ActivePlayer.Deck, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(context.ActivePlayer, goldPile);
                    }
                }
            }
        }
        #endregion
    }
}