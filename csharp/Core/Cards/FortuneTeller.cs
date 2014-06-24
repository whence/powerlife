using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 3, true)]
    public class FortuneTeller : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                using (var transZone = new TransitionalZone(player))
                {
                    var cardValidator = new CardTypeValidator<IVictoryCard, Curse>();
                    player.MoveFromTopDeckTill(cardValidator, 1, transZone, CardMovementVerb.Reveal, context);
                    transZone.Reveal(context.Game.Log);

                    var putBackCard = transZone.SingleOrDefault(cardValidator.Validate);
                    if (putBackCard != null)
                    {
                        if (putBackCard.MoveTo(transZone, player.Deck, CardMovementVerb.PutBackToDeck, context))
                            context.Game.Log.LogMessage("{0} put {1} back to top deck", player.Name, putBackCard.Name);
                    }
                    transZone.MoveAll(transZone, player.DiscardArea, CardMovementVerb.Discard, context);
                }
            }
        }
        #endregion
    }
}
