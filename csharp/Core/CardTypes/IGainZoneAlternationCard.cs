namespace Powercards.Core
{
    public interface IGainZoneAlternationCard : ICard
    {
        #region methods
        bool IsAlternationUsable(Player player);
        ICardZone ResolveCardZone(TurnContext context, Player player, ICard card, ICardZone targetZone);
        #endregion
    }
}
