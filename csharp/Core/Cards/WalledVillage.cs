using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Promo, 4, true)]
    public class WalledVillage : CardBase, IActionCard, IPreCleanupSelfMovementCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;
        }

        public bool ShouldMove(TurnContext context, Player player)
        {
            if (this.CurrentZone == player.PlayArea && player.PlayArea.OfType<IActionCard>().Count() <= 2)
            {
                return context.Game.Dialog.Should(context, player, "Put Walled Village back on top?");
            }
            return false;
        }

        public void MoveBeforeCleanup(TurnContext context, Player player)
        {
            if (this.MoveTo(context.ActivePlayer.PlayArea, context.ActivePlayer.Deck, CardMovementVerb.PutBackToDeck, context))
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " put a Walled Village back on top deck");
        }
        #endregion
    }
}
