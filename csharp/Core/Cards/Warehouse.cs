using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 3, true)]
    public class Warehouse : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(3, context);
            context.RemainingActions += 1;

            ICard[] discardCards;
            if (context.ActivePlayer.Hand.CardCount > 3)
            {
                discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(3), "Select 3 cards to discard.");
            }
            else
            {
                discardCards = context.ActivePlayer.Hand.ToArray();
            }
            discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            context.Game.Log.LogMessage("{0} discarded {1} cards", context.ActivePlayer.Name, discardCards.Length);
        }
        #endregion
    }
}
