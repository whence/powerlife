namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 3, true)]
    public class FishingVillage : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 2;
            context.AvailableSpend += 1;
            
            context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            context.RetainCardInPlay(this);
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.RemainingActions += 1;
            context.AvailableSpend += 1;
            context.Game.Log.LogMessage("FishingVillage adds one action and one spend");
        }
        #endregion
    }
}
