using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class ShantyTown : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 2;

            context.Game.Log.LogRevealHand(context.ActivePlayer);
            
            if (!context.ActivePlayer.Hand.OfType<IActionCard>().Any())
            {
                context.ActivePlayer.DrawCards(2, context);
            }
        }
        #endregion
    }
}
