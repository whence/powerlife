using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class TradingPost : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
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
                        new CountValidator<ICard>(2), "Select 2 cards to trash");
                    break;
            }
            trashCards.MoveAll(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context);
            foreach (var card in trashCards)
            {
                context.Game.Log.LogTrash(context.ActivePlayer, card);
            }

            switch (trashCards.Length)
            {
                case 0:
                    context.Game.Log.LogMessage(context.ActivePlayer.Name + " trashed nothing, gain nothing");
                    break;

                case 2:
                    var silverPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Silver>()).Validate);
                    if (silverPile != null)
                    {
                        if (silverPile.TopCard.MoveTo(silverPile, context.ActivePlayer.Hand, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(context.ActivePlayer, silverPile);
                    }
                    break;
            }
        }
        #endregion
    }
}