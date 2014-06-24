using IdeaFactory.Util;

namespace Powercards.Core
{
    public class CardZoneValidator : CompositeValidatorBase<ICard>
    {
        #region fields
        private readonly ICardZone zone;
        #endregion

        #region constructors
        public CardZoneValidator(ICardZone zone)
        {
            Enforce.ArgumentNotNull(zone);
            this.zone = zone;
        }
        #endregion

        #region methods
        public override bool Validate(ICard card)
        {
            if (!base.Validate(card))
                return false;

            return card.CurrentZone == zone;
        }
        #endregion
    }
}
