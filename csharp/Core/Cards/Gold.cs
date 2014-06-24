namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 6, false)]
    public class Gold : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 3);
        }
        #endregion
    }
}