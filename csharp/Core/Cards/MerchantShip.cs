namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class MerchantShip : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            context.RetainCardInPlay(this);
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.AvailableSpend += 2;
        }
        #endregion
    }
}