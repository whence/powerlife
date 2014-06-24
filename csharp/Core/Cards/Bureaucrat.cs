using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Bureaucrat : CardBase, IActionCard 
    {
        #region methods
        public void Play(TurnContext context)
        {
            var silverPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Silver>()).Validate);
            if (silverPile != null)
            {
                if (silverPile.TopCard.MoveTo(silverPile, context.ActivePlayer.Deck, CardMovementVerb.Gain, context))
                    context.Game.Log.LogGain(context.ActivePlayer, silverPile);
            }

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                if (player.Hand.OfType<IVictoryCard>().Any())
                {
                    var cardToPutBack = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(1).AndEach(new CardTypeValidator<IVictoryCard>()), 
                        "Select a victory card to put on top").Single();

                    if (cardToPutBack.MoveTo(player.Hand, player.Deck, CardMovementVerb.PutBackToDeck, context))
                        context.Game.Log.LogPutBack(player, cardToPutBack);
                }
                else
                {
                    context.Game.Log.LogRevealHand(player);
                }
            }
        }
        #endregion
    }
}