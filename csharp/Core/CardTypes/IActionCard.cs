namespace Powercards.Core
{
    public interface IActionCard : ICard
    {
        #region methods
        void Play(TurnContext context);
        #endregion
    }
}