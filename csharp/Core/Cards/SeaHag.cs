using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class SeaHag : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                player.MoveFromTopDeck(1, player.DiscardArea, CardMovementVerb.Discard, context);

                var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                if (cursePile != null)
                {
                    if (cursePile.TopCard.MoveTo(cursePile, player.Deck, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(player, cursePile);
                }
            }
        }
        #endregion
    }
}
