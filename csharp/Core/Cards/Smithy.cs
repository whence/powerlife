namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Smithy : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(3, context);
        }
        #endregion
    }
}
