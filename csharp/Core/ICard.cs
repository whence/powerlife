namespace Powercards.Core
{
    public interface ICard : INameIdentifiable
    {
        #region properties
        ICardZone CurrentZone { get; }
        #endregion

        #region methods
        int GetCost(TurnContext context, Player player);

        /// <returns>False if the move is altered</returns>
        bool MoveTo(ICardZone sourceZone, ICardZone targetZone, CardMovementVerb verb, TurnContext context);
        #endregion
    }
}
