namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 9, false)]
    public class Platinum : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 5);
        }
        #endregion
    }
}