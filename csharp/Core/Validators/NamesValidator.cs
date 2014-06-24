using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class NamesValidator<T> : CompositeValidatorBase<T>
        where T : INameIdentifiable
    {
        #region fields
        private readonly IEnumerable<string> names;
        #endregion

        #region constructors
        public NamesValidator(IEnumerable<string> names)
        {
            Enforce.ArgumentNotNull(names);
            this.names = names;
        }
        #endregion

        #region methods
        public override bool Validate(T item)
        {
            if (!base.Validate(item))
                return false;

            return names.Contains(item.Name, StringComparer.Ordinal);
        }
        #endregion
    }
}
