using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 3, true)]
    public class TradeRoute : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;

            context.AvailableSpend += context.Game.TradeRouteTokens;

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Trash a card from hand").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);
            }
        }
        #endregion
    }
}
