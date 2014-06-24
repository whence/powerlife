namespace Powercards.Core
{
    public interface IDurationEffect
    {
        #region methods
        void OnTurnStarting(TurnContext context);
        #endregion
    }
}
