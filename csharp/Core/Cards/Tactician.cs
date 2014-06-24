namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Tactician : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                context.ActivePlayer.Hand.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);

                context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
                context.RetainCardInPlay(this);
            }
            else
            {
                context.Game.Log.LogMessage("{0} did not have any cards to discard to the Tactician.", context.ActivePlayer.Name);

                // we don't add duration effect, because if the player has nothing to discard, Tactician's duration effect is not activated.
            }
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.ActivePlayer.DrawCards(5, context);
            context.Buys += 1;
            context.RemainingActions += 1;
            context.Game.Log.LogMessage("Tactician gives {0} +5 cards, +1 buy and +1 action.", context.ActivePlayer.Name);
        }
        #endregion
    }
}
