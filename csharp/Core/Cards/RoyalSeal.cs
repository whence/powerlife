namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class RoyalSeal : CardBase, ITreasureCard, IGainZoneAlternationCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 2);
        }

        public bool IsAlternationUsable(Player player)
        {
            return (this.CurrentZone == player.PlayArea);
        }

        public ICardZone ResolveCardZone(TurnContext context, Player player, ICard card, ICardZone targetZone)
        {
            context.Game.Log.LogGain(player, card);
            context.Game.Log.LogPutBack(player, card);
            return player.Deck;
        }
        #endregion
    }
}
