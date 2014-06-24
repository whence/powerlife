namespace Powercards.Core
{
    public interface IDiscardSelfZoneAlternationCard : ICard
    {
        #region methods
        ICardZone ResolveCardZone(TurnContext context, Player player, ICardZone targetZone);
        #endregion
    }
}
