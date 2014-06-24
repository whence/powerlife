using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 3, true)]
    public class Workshop : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            const int maxCostOfCardToGain = 4;
            var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, 0, maxCostOfCardToGain)).Validate).ToArray();
            if (pilesToGain.Length > 0)
            {
                var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain, 
                    new CountValidator<CardSupplyPile>(1), 
                    string.Format("Select a card to gain of cost {0} or less.", maxCostOfCardToGain)).Single();

                if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                    context.Game.Log.LogGain(context.ActivePlayer, gainPile);
            }
            else
            {
                context.Game.Log.LogMessage("{0} could gain no card of appropriate cost", context.ActivePlayer);
            }
        }
        #endregion
    }
}