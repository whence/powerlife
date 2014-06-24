namespace Powercards.Core
{
    public interface IPreCleanupSelfMovementCard : ICard
    {
        #region methods
        bool ShouldMove(TurnContext context, Player player);
        void MoveBeforeCleanup(TurnContext context, Player player);
        #endregion
    }
}
