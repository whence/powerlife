namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Laboratory : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;
            context.ActivePlayer.DrawCards(2, context);
        }
        #endregion
    }
}
