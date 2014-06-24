using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class Salvager : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to trash").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                context.AvailableSpend += trashCard.GetCost(context, context.ActivePlayer);
            }
        }
        #endregion
    }
}
