using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 5, true)]
    public class Jester : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                var discardedCard = player.MoveOneFromTopDeck(player.DiscardArea, CardMovementVerb.Discard, context);

                if (discardedCard != null)
                {
                    if (new CardTypeValidator<IVictoryCard>().Validate(discardedCard))
                    {
                        var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                        if (cursePile != null)
                        {
                            if (cursePile.TopCard.MoveTo(cursePile, player.DiscardArea, CardMovementVerb.Gain, context))
                                context.Game.Log.LogGain(player, cursePile);
                        }
                    }
                    else
                    {
                        var gainPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new NameValidator<ICard>(discardedCard.Name)).Validate);
                        if (gainPile != null)
                        {
                            var gainer = (context.Game.Dialog.Should(context, context.ActivePlayer, "Woule you like to gain a copy of " + discardedCard.Name) ? context.ActivePlayer : player);
                            
                            if (gainPile.TopCard.MoveTo(gainPile, gainer.DiscardArea, CardMovementVerb.Gain, context))
                                context.Game.Log.LogGain(gainer, gainPile);
                        }
                        else
                        {
                            context.Game.Log.LogMessage("There is no appropiate card to gain in the supply");
                        }
                    }
                }
                else
                {
                    context.Game.Log.LogMessage(player.Name + " has no card to discard");
                }
            }
        }
        #endregion
    }
}
