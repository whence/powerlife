namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 3, true)]
    public class Village : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;
        }
        #endregion
    }
}