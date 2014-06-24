using IdeaFactory.Util;

namespace Powercards.Core
{
    public class CardCostValidator : CompositeValidatorBase<ICard>
    {
        #region fields
        private readonly TurnContext context;
        private readonly Player player;
        #endregion

        #region properties
        public int? MinCost { get; private set; }
        public int? MaxCost { get; private set; }
        #endregion

        #region constructors
        public CardCostValidator(TurnContext context, Player player, int exactCost)
        {
            Enforce.ArgumentNotNull(context);
            this.context = context;

            Enforce.ArgumentNotNull(player);
            this.player = player;
            
            this.MinCost = exactCost;
            this.MaxCost = exactCost;
        }

        public CardCostValidator(TurnContext context, Player player, int? minCost, int? maxCost)
        {
            Enforce.ArgumentNotNull(context);
            this.context = context;

            Enforce.ArgumentNotNull(player);
            this.player = player;
            
            this.MinCost = minCost;
            this.MaxCost = maxCost;
        }
        #endregion

        #region methods
        public override bool Validate(ICard card)
        {
            if (!base.Validate(card))
                return false;

            if (MinCost.HasValue && card.GetCost(context, player) < MinCost)
                return false;

            if (MaxCost.HasValue && card.GetCost(context, player) > MaxCost)
                return false;

            return true;
        }
        #endregion
    }
}
