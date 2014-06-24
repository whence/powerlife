using System.Collections.Generic;

namespace Powercards.Core
{
    public interface IGameDialog
    {
        T[] Select<T>(TurnContext context, Player player, IEnumerable<T> items, IValidator<IEnumerable<T>> validator, string description) where T : INameIdentifiable;
        bool Should(TurnContext context, Player player, string description);
        int[] Choose(TurnContext context, Player player, string[] choices, int numberOfChoicesToChoose, string description);
        string Name(TurnContext context, Player player, string description);
    }
}
