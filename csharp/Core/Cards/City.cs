using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class City : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;

            var emptyPileCount = context.Game.Supply.AllPiles.Count(new InvertValidator<CardSupplyPile>(new NonEmptyPileValidator()).Validate);
            if (emptyPileCount >= 1)
            {
                context.ActivePlayer.DrawCards(1, context);
            }
            if (emptyPileCount >= 2)
            {
                context.AvailableSpend += 1;
                context.Buys += 1;
            }
        }
        #endregion
    }
}