namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class CouncilRoom : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(4, context);
            context.Buys += 1;

            foreach (var player in context.Opponents)
            {
                player.DrawCards(1, context);
            }
        }
        #endregion
    }
}
