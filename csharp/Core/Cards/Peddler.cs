using System;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 8, true)]
    public class Peddler : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            context.AvailableSpend += 1;
        }

        public override int GetCost(TurnContext context, Player player)
        {
            if (context.Stage == TurnContext.TurnStage.Buy)
                return context.CalculateCardCost(this,
                    Math.Max(this.OriginalCost - player.PlayArea.OfType<IActionCard>().Count() * 2, 0), player);
            
            return base.GetCost(context, player);
        }
        #endregion
    }
}
