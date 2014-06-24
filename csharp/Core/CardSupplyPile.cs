using IdeaFactory.Util;

namespace Powercards.Core
{
    public class CardSupplyPile : CardZoneBase, INameIdentifiable
    {
        #region fields
        private readonly string name;
        private readonly bool isGameEndingPile;
        #endregion

        #region properties
        public ICard TopCard
        {
            get { return IsEmpty ? null : this.GetCard(this.CardCount - 1); }
        }

        public int EmbargoTokens { get; private set; }
        public bool HasTradeRouteToken { get; private set; }
        #endregion

        #region properties
        public string Name
        {
            get { return name; }
        }

        public bool IsGameEndingPile
        {
            get { return isGameEndingPile; }
        }
        #endregion

        #region constructors
        public CardSupplyPile(string name, bool isGameEndingPile, bool hasTradeRouteToken)
        {
            Enforce.ArgumentNotEmptyOrNull(name);
            this.name = name;

            this.isGameEndingPile = isGameEndingPile;
            this.HasTradeRouteToken = hasTradeRouteToken;
        }
        #endregion

        #region methods
        public void Embargo()
        {
            this.EmbargoTokens += 1;
        }

        public void MoveTradeRouteToken(Game game)
        {
            if (this.HasTradeRouteToken)
            {
                game.TradeRouteTokens += 1;
                this.HasTradeRouteToken = false;
            }
        }
        #endregion
    }
}