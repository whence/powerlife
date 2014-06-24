using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Mountebank : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;
            
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                var discardCurse = player.Hand.OfType<Curse>().FirstOrDefault();
                if (discardCurse != null
                    && context.Game.Dialog.Should(context, player, "Discard a curse?"))
                {
                    if (discardCurse.MoveTo(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context))
                        context.Game.Log.LogDiscard(player, discardCurse);
                }
                else
                {
                    var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                    if (cursePile != null)
                    {
                        if (cursePile.TopCard.MoveTo(cursePile, player.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(player, cursePile);
                    }

                    var copperPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Copper>()).Validate);
                    if (copperPile != null)
                    {
                        if (copperPile.TopCard.MoveTo(copperPile, player.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(player, copperPile);
                    }
                }
            }
        }
        #endregion
    }
}