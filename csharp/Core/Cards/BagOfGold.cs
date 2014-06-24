using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 0, false)]
    public class BagOfGold : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            var goldPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Gold>()).Validate);
            if (goldPile != null)
            {
                if (goldPile.TopCard.MoveTo(goldPile, context.ActivePlayer.Deck, CardMovementVerb.Gain, context))
                    context.Game.Log.LogGain(context.ActivePlayer, goldPile);
            }
        }
        #endregion
    }
}
