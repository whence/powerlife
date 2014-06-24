namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class Caravan : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            
            context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            context.RetainCardInPlay(this);
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.Game.Log.LogMessage("Caravan draws an extra card");
        }
        #endregion
    }
}
