namespace Powercards.Core
{
    public interface IInPlayCostModifierCard : ICard
    {
        #region methods
        int OnEvalCardCost(ICard card, int cost);
        #endregion
    }
}
