using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 3, true)]
    public class Menagerie : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            context.Game.Log.LogRevealHand(context.ActivePlayer);

            if (context.ActivePlayer.Hand.DistinctByName().Count() == context.ActivePlayer.Hand.CardCount)
            {
                context.ActivePlayer.DrawCards(3, context);
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " has no duplicate card in hand so he draws 3 cards");
            }
            else
            {
                context.ActivePlayer.DrawCards(1, context);
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " has duplicate cards in hand so he draws 1 cards");
            }
        }
        #endregion
    }
}
