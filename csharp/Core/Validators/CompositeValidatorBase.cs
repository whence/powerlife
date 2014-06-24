namespace Powercards.Core
{
    public abstract class CompositeValidatorBase<T> : IValidator<T>
    {
        #region fields
        private IValidator<T> inner;
        #endregion

        #region methods
        public virtual bool Validate(T obj)
        {
            if (this.inner != null)
            {
                if (!this.inner.Validate(obj))
                    return false;
            }
            return true;
        }

        /// <returns>using AND means the ordering doesn't matter</returns>
        public CompositeValidatorBase<T> And(IValidator<T> other)
        {
            this.inner = other;
            return this;
        }
        #endregion
    }
}
