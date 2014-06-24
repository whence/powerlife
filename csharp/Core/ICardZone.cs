namespace Powercards.Core
{
    public interface ICardZone
    {
        void AddCard(ICard card, CardMovementVerb verb);
        void RemoveCard(ICard card, CardMovementVerb verb);
    }
}
