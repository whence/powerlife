using System;
using System.Collections.Generic;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Vault : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);
            
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new NullValidator<IEnumerable<ICard>>(), "Select any number of cards to discard, you will gain $1 per discard");

                if (discardCards.Length > 0)
                {
                    discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
                    context.Game.Log.LogMessage("{0} discarded {1} cards", context.ActivePlayer.Name, discardCards.Length);
                    context.AvailableSpend += discardCards.Length;
                }
            }

            foreach (var player in context.Opponents)
            {
                if (!player.Hand.IsEmpty)
                {
                    var discardCards = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(new[] { 0, Math.Min(2, player.Hand.CardCount), }),
                        "Select 2 cards to discard, or none to skip");

                    if (discardCards.Length > 0)
                    {
                        discardCards.MoveAll(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context);
                        context.Game.Log.LogMessage("{0} discarded {1} cards", player.Name, discardCards.Length);
                        
                        if (discardCards.Length == 2)
                        {
                            player.DrawCards(1, context);
                        }
                    }
                }
            }
        }
        #endregion
    }
}
