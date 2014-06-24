namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Bazaar : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;
            context.AvailableSpend += 1;
        }
        #endregion
    }
}
