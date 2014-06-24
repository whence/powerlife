namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class Tribute : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            // note: this is likely also NOT attack, similar to masquerade
            var nextPlayer = context.Game.NextPlayerOf(context.ActivePlayer);
            using (var transZone = new TransitionalZone(nextPlayer))
            {
                nextPlayer.MoveFromTopDeck(2, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                foreach (var card in transZone.DistinctByName())
                {
                    if (card is IActionCard) context.RemainingActions += 2;
                    if (card is ITreasureCard) context.AvailableSpend += 2;
                    if (card is IVictoryCard) context.ActivePlayer.DrawCards(2, context);
                }
                transZone.MoveAll(transZone, nextPlayer.DiscardArea, CardMovementVerb.Discard, context);
            }
        }
        #endregion
    }
}