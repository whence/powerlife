using System.Collections.Generic;

namespace Powercards.Core
{
    public class EnumerableValidator<T> : CompositeValidatorBase<IEnumerable<T>>
    {
        #region fields
        private IValidator<T> innerEach;
        #endregion

        #region methods
        public override bool Validate(IEnumerable<T> items)
        {
            if (!base.Validate(items))
                return false;

            if (this.innerEach != null)
            {
                foreach (var item in items)
                {
                    if (!this.innerEach.Validate(item))
                        return false;
                }
            }
            return true;
        }

        public EnumerableValidator<T> AndEach(IValidator<T> other)
        {
            this.innerEach = other;
            return this;
        }
        #endregion
    }
}
