namespace Powercards.Core
{
    public class CardTypeValidator<T> : CompositeValidatorBase<ICard>
    {
        #region methods
        public override bool Validate(ICard card)
        {
            if (!base.Validate(card))
                return false;

            return (card is T);
        }
        #endregion
    }

    /// <summary>
    /// Verify a card is either T1 or T2
    /// </summary>
    public class CardTypeValidator<T1, T2> : CompositeValidatorBase<ICard>
    {
        #region methods
        public override bool Validate(ICard card)
        {
            if (!base.Validate(card))
                return false;

            return ((card is T1) || (card is T2));
        }
        #endregion
    }
}
