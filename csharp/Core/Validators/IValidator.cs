namespace Powercards.Core
{
    public interface IValidator<T>
    {
        bool Validate(T obj);
    }
}
