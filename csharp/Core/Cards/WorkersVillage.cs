namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 4, true)]
    public class WorkersVillage : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;
            context.Buys += 1;
        }
        #endregion
    }
}