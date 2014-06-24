namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Treasury : CardBase, IActionCard, IDiscardSelfZoneAlternationCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            context.AvailableSpend += 1;
        }

        public ICardZone ResolveCardZone(TurnContext context, Player player, ICardZone targetZone)
        {
            if (this.CurrentZone == player.PlayArea)
            {
                if (context.HasBoughtVictoryCardThisTurn(player))
                {
                    if (context.Game.Dialog.Should(context, player, "Put Treasury on top of your deck?"))
                    {
                        context.Game.Log.LogPutBack(player, this);
                        return player.Deck;
                    }
                }
            }
            return targetZone;
        }
        #endregion
    }
}
