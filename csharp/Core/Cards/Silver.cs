namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 3, false)]
    public class Silver : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 2);
        }
        #endregion
    }
}