using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 2, true)]
    public class Cellar : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            var discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                new NullValidator<IEnumerable<ICard>>(),
                "Select any number of cards to discard, you will draw 1 new card for each discard.");
            discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            
            context.ActivePlayer.DrawCards(discardCards.Length, context);
            context.Game.Log.LogMessage("{0} discards {1} cards then draw {1} cards.", context.ActivePlayer.Name, discardCards.Length);
        }
        #endregion
    }
}
