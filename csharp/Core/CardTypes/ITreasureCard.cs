namespace Powercards.Core
{
    public interface ITreasureCard : ICard
    {
        #region methods
        void PlayAndProduceValue(TurnContext context);
        #endregion
    }
}