namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Wharf : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);
            context.Buys += 1;

            context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            context.RetainCardInPlay(this);
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);
            context.Buys += 1;
            context.Game.Log.LogMessage("{0} gains 2 cards and 1 buy from Wharf", context.ActivePlayer.Name);
        }
        #endregion
    }
}