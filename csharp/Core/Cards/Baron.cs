using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class Baron : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;

            var estate = context.ActivePlayer.Hand.OfType<Estate>().FirstOrDefault();
            if (estate != null && context.Game.Dialog.Should(context, context.ActivePlayer, "Discard an Estate?"))
            {
                context.AvailableSpend += 4;
                if (estate.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context))
                    context.Game.Log.LogDiscard(context.ActivePlayer, estate);
            }
            else
            {
                var estatePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Estate>()).Validate);
                if (estatePile != null)
                {
                    if (estatePile.TopCard.MoveTo(estatePile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(context.ActivePlayer, estatePile);
                }
            }
        }
        #endregion
    }
}