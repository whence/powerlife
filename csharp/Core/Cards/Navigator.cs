namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class Navigator : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeck(5, transZone, CardMovementVerb.Lookat, context);

                if (!transZone.IsEmpty)
                {
                    var putBackCards = context.Game.Dialog.Select(context, context.ActivePlayer, transZone,
                    new CountValidator<ICard>(new[] { 0, transZone.CardCount, }),
                    "Select cards to put back, last one on top. Or none to discard them all");

                    if (putBackCards.Length > 0)
                    {
                        putBackCards.MoveAll(transZone, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context);
                        context.Game.Log.LogMessage("{0} put {1} cards back to deck", context.ActivePlayer.Name, putBackCards.Length);
                    }
                    else if (!transZone.IsEmpty)
                    {
                        transZone.MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " discards all looked at cards");
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
