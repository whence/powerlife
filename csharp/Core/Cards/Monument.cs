namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 4, true)]
    public class Monument : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;
            context.ActivePlayer.VPTokens += 1;
        }
        #endregion
    }
}
