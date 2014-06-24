using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class Steward : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            switch (context.Game.Dialog.Choose(context, context.ActivePlayer,
                new[] { "+2 card", "+$2", "Trash 2 cards from hand", }, 1, "Choose one")[0])
            {
                case 0:
                    context.ActivePlayer.DrawCards(2, context);
                    break;

                case 1:
                    context.AvailableSpend += 2;
                    break;

                case 2:
                    ICard[] trashCards;
                    switch (context.ActivePlayer.Hand.CardCount)
                    {
                        case 0:
                        case 1:
                        case 2:
                            trashCards = context.ActivePlayer.Hand.ToArray();
                            break;

                        default:
                            trashCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                                new CountValidator<ICard>(2), "Select 2 cards to trash.");
                            break;
                    }

                    if (trashCards.Length > 0)
                    {
                        trashCards.MoveAll(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context);
                        foreach (var card in trashCards)
                        {
                            context.Game.Log.LogTrash(context.ActivePlayer, card);
                        }
                    }
                    else
                    {
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " has no card to trash");
                    }
                    break;
            }
        }
        #endregion
    }
}
