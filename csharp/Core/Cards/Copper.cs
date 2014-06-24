namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 0, false)]
    public class Copper : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 1);
        }
        #endregion
    }
}