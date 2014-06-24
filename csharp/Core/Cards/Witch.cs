using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Witch : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);
            
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                if (cursePile != null)
                {
                    if (cursePile.TopCard.MoveTo(cursePile, player.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(player, cursePile);
                }
            }
        }
        #endregion
    }
}
