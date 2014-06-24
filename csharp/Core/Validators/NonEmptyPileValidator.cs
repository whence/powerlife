namespace Powercards.Core
{
    public class NonEmptyPileValidator : CompositeValidatorBase<CardSupplyPile>
    {
        #region fields
        private IValidator<ICard> innerCard;
        #endregion

        #region methods
        public override bool Validate(CardSupplyPile pile)
        {
            if (!base.Validate(pile))
                return false;

            if (pile.IsEmpty)
                return false;

            if (this.innerCard != null)
            {
                if (!this.innerCard.Validate(pile.TopCard))
                    return false;
            }
            return true;
        }

        /// <returns>using THEN means will do self's validation, then the other's</returns>
        public CompositeValidatorBase<CardSupplyPile> Then(IValidator<ICard> other)
        {
            this.innerCard = other;
            return this;
        }
        #endregion
    }
}
