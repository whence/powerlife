namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Market : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            context.Buys += 1;
            context.AvailableSpend += 1;
        }
        #endregion
    }
}
