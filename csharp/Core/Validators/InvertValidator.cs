using IdeaFactory.Util;

namespace Powercards.Core
{
    public class InvertValidator<T> : IValidator<T>
    {
        #region fields
        private readonly IValidator<T> inner;
        #endregion

        #region constructors
        public InvertValidator(IValidator<T> inner)
        {
            Enforce.ArgumentNotNull(inner);
            this.inner = inner;
        }
        #endregion

        #region methods
        public bool Validate(T obj)
        {
            return !this.inner.Validate(obj);
        }
        #endregion
    }
}
