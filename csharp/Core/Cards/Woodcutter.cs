namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 3, true)]
    public class Woodcutter : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;
            context.AvailableSpend += 2;
        }
        #endregion
    }
}