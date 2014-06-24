namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Outpost : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (context.RequestExtraTurnForActivePlayer())
            {
                context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
                context.RetainCardInPlay(this);
            }
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.Game.Log.LogMessage(context.ActivePlayer.Name + " is granted an extra turn by Outpost");
        }
        #endregion
    }
}
