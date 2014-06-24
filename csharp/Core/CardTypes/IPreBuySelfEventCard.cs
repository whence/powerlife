namespace Powercards.Core
{
    public interface IPreBuySelfEventCard : ICard
    {
        #region properties
        string BuyConditionDescription { get; }
        #endregion

        #region methods
        bool MeetBuyCondition(TurnContext context, Player player);
        void BeforeBuy(TurnContext context, Player player);
        #endregion
    }
}
