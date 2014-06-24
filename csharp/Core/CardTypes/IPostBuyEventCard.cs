namespace Powercards.Core
{
    public interface IPostBuyOtherCardEventCard : ICard
    {
        #region methods
        void AfterBuy(TurnContext context, Player player, ICard card);
        #endregion
    }
}
