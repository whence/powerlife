using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class ThroneRoom : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (context.ActivePlayer.Hand.OfType<IActionCard>().Any())
            {
                var actionCard = (IActionCard)context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1).AndEach(new CardTypeValidator<IActionCard>()),
                    "Please select an action card to play twice").Single();

                Enforce.IsTrue(actionCard.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.PlayArea, CardMovementVerb.Play, context));

                context.PushActionPlayChainForActivePlayer(this);
                for (int i = 0; i < 2; i++)
                {
                    context.Game.Log.LogPlay(context.ActivePlayer, actionCard);
                    context.PlayedActions += 1;
                    actionCard.Play(context);
                }
                context.PopActionPlayChainForActivePlayer();
            }
            else
            {
                context.Game.Log.LogMessage("{0} did not have any actions to use Throne Room on.", context.ActivePlayer.Name);
            }
        }
        #endregion
    }
}
