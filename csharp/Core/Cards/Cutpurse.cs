using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class Cutpurse : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                var copper = player.Hand.OfType<Copper>().FirstOrDefault();
                if (copper != null)
                {
                    if (copper.MoveTo(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context))
                        context.Game.Log.LogDiscard(player, copper);
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