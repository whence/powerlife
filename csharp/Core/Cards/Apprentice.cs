using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Alchemy, 5, true)]
    public class Apprentice : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to trash").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                context.ActivePlayer.DrawCards(trashCard.GetCost(context, context.ActivePlayer), context);
            }
            else
            {
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " did not have any cards to trash");
            }
        }
        #endregion
    }
}
