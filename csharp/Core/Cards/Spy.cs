using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Spy : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            
            foreach (var player in context.Players)
            {
                if (player != context.ActivePlayer && !player.OnAttack(context))
                    continue;
                
                using (var transZone = new TransitionalZone(player))
                {
                    player.MoveFromTopDeck(1, transZone, CardMovementVerb.Reveal, context);
                    transZone.Reveal(context.Game.Log);

                    if (!transZone.IsEmpty)
                    {
                        var revealCard = transZone.Single();
                        if (context.Game.Dialog.Should(context, context.ActivePlayer,
                            string.Format("Discard {0} {1}?", (player == context.ActivePlayer ? "Your" : player.Name + "'s"),
                            revealCard)))
                        {
                            if (revealCard.MoveTo(transZone, player.DiscardArea, CardMovementVerb.Discard, context))
                                context.Game.Log.LogDiscard(player, revealCard);
                        }
                        else
                        {
                            if (revealCard.MoveTo(transZone, player.Deck, CardMovementVerb.PutBackToDeck, context))
                                context.Game.Log.LogPutBack(player, revealCard);
                        }
                    }
                }
            }
        }
        #endregion
    }
}