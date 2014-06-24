using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 2, true)]
    public class PearlDiver : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromBottomDeck(1, transZone, CardMovementVerb.Lookat, context);

                if (!transZone.IsEmpty)
                {
                    var lookatCard = transZone.Single();
                    if (context.Game.Dialog.Should(context, context.ActivePlayer, "Would you like to put " + lookatCard.Name + " on top of your deck"))
                    {
                        if (lookatCard.MoveTo(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context))
                            context.Game.Log.LogMessage(context.ActivePlayer.Name + " put bottom card of deck on top");
                    }
                    else
                    {
                        if (lookatCard.MoveTo(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeckBottom, context))
                            context.Game.Log.LogMessage(context.ActivePlayer.Name + " put bottom card of deck back to bottom");
                    }
                }
                else
                {
                    context.Game.Log.LogMessage("There is no card in the deck to look at");
                }
            }
        }
        #endregion
    }
}
