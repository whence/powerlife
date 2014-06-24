namespace Powercards.Core
{
    public class NullValidator<T> : IValidator<T>
    {
        public bool Validate(T obj)
        {
            return true;
        }
    }
}
