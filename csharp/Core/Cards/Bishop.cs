using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 4, true)]
    public class Bishop : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 1;
            context.ActivePlayer.VPTokens += 1;

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to trash").Single();

                if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, trashCard);

                context.ActivePlayer.VPTokens += Toolbox.RoundDown(trashCard.GetCost(context, context.ActivePlayer), 2);
            }

            foreach (var player in context.Opponents)
            {
                if (!player.Hand.IsEmpty)
                {
                    var trashCard = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(0, 1), "You may trash a card in hand").SingleOrDefault();

                    if (trashCard != null)
                    {
                        if (trashCard.MoveTo(player.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                            context.Game.Log.LogTrash(player, trashCard);
                    }
                }
            }
        }
        #endregion
    }
}
