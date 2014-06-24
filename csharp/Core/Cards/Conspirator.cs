namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class Conspirator : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            if (context.PlayedActions >= 3)
            {
                context.ActivePlayer.DrawCards(1, context);
                context.RemainingActions += 1;
            }
        }
        #endregion
    }
}
