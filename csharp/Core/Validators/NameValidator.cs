using System;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class NameValidator<T> : CompositeValidatorBase<T>
        where T : INameIdentifiable
    {
        #region fields
        private readonly string name;
        #endregion

        #region constructors
        public NameValidator(string name)
        {
            Enforce.ArgumentNotEmptyOrNull(name);
            this.name = name;
        }
        #endregion

        #region methods
        public override bool Validate(T item)
        {
            if (!base.Validate(item))
                return false;

            return item.Name.Equals(name, StringComparison.Ordinal);
        }
        #endregion
    }
}
