using System;
using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core
{
    public class CountValidator<T> : EnumerableValidator<T>
    {
        #region fields
        public readonly int? minCount;
        public readonly int? maxCount;
        public readonly int[] acceptableCounts;
        #endregion

        #region constructors
        public CountValidator(int exactCount)
        {
            this.minCount = exactCount;
            this.maxCount = exactCount;
            this.acceptableCounts = null;
        }

        public CountValidator(int? minCount, int? maxCount)
        {
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.acceptableCounts = null;
        }

        public CountValidator(int[] acceptableCounts)
        {
            this.minCount = null;
            this.maxCount = null;
            this.acceptableCounts = acceptableCounts;
        }
        #endregion

        #region methods
        public override bool Validate(IEnumerable<T> items)
        {
            if (!base.Validate(items))
                return false;

            var count = items.Count();
            
            if (this.minCount.HasValue && count < this.minCount)
                return false;

            if (this.maxCount.HasValue && count > this.maxCount)
                return false;

            if (this.acceptableCounts != null && Array.IndexOf(this.acceptableCounts, count) == -1)
                return false;

            return true;
        }
        #endregion
    }
}
