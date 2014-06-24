using System.Collections;
using System.Collections.Generic;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class GenericEnumerable<T> : IEnumerable<T>
    {
        #region fields
        private readonly IEnumerator<T> enumerator;
        #endregion

        #region constructors
        public GenericEnumerable(IEnumerator<T> enumerator)
        {
            Enforce.ArgumentNotNull(enumerator);
            this.enumerator = enumerator;
        }
        #endregion

        #region methods
        public IEnumerator<T> GetEnumerator()
        {
            return enumerator;
        }
        #endregion

        #region interface members
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
        #endregion
    }
}
