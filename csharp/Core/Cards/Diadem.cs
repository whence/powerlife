namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 0, false)]
    public class Diadem : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 2);
            context.AvailableSpend += context.UnusedActions;
        }
        #endregion
    }
}
