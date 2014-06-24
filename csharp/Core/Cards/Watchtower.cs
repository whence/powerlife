namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 3, true)]
    public class Watchtower : CardBase, IActionCard, IGainZoneAlternationCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCardsTill(6, context);
        }

        public bool IsAlternationUsable(Player player)
        {
            return (this.CurrentZone == player.Hand);
        }

        public ICardZone ResolveCardZone(TurnContext context, Player player, ICard card, ICardZone targetZone)
        {
            switch (context.Game.Dialog.Choose(context, player, new[] { 
                    "Trash " + card.Name, 
                    "Put " + card.Name + " on top of your deck", 
                    }, 1, "Choose one")[0])
            {
                case 0:
                    context.Game.Log.LogGain(player, card);
                    context.Game.Log.LogTrash(player, card);
                    return context.Game.TrashZone;

                case 1:
                    context.Game.Log.LogGain(player, card);
                    context.Game.Log.LogPutBack(player, card);
                    return player.Deck;

                default:
                    return targetZone;
            }
        }
        #endregion
    }
}
