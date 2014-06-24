namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class Minion : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            if (context.Game.Dialog.Should(context, context.ActivePlayer, "Discard your hand?"))
            {
                foreach (var player in context.Players)
                {
                    if (player != context.ActivePlayer && !player.OnAttack(context))
                        continue;
                    
                    if (player == context.ActivePlayer || player.Hand.CardCount > 4)
                    {
                        player.Hand.MoveAll(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context);
                        player.DrawCards(4, context);
                        context.Game.Log.LogMessage(player.Name + " discarded hand and draw 4 cards");
                    }
                }
            }
            else
            {
                context.AvailableSpend += 2;
            }
        }
        #endregion
    }
}