using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 3, true)]
    public class Smugglers : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            var previousPlayer = context.Opponents.LastOrDefault();
            if (previousPlayer != null)
            {
                var pilesToGain = context.Game.Supply.AllPiles
                    .Where(previousPlayer.HasRecentlyGainedOnSelfTurn)
                    .Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, 0, 6)).Validate)
                    .ToArray();

                if (pilesToGain.Length > 0)
                {
                    var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain,
                        new CountValidator<CardSupplyPile>(1), "Select a card to smuggle").Single();

                    if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(context.ActivePlayer, gainPile);
                }
                else
                {
                    context.Game.Log.LogMessage("There are no card suitable for {0} to gain", context.ActivePlayer);
                }
            }
        }
        #endregion
    }
}
