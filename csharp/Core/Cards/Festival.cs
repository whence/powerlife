namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Festival : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 2;
            context.Buys += 1;
            context.AvailableSpend += 2;
        }
        #endregion
    }
}
