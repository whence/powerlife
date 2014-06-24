using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class Island : CardBase, IActionCard, IVictoryCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (this.CurrentZone != context.ActivePlayer.SetAsideArea)
            {
                if (this.MoveTo(context.ActivePlayer.PlayArea, context.ActivePlayer.SetAsideArea, CardMovementVerb.SetAside, context))
                    context.Game.Log.LogMessage(context.ActivePlayer.Name + " set aside an Island");
            }

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var setAsideCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to set aside.").Single();

                if (setAsideCard.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.SetAsideArea, CardMovementVerb.SetAside, context))
                    context.Game.Log.LogMessage(context.ActivePlayer.Name + " set aside a " + setAsideCard.Name);
            }
        }

        public int Score(IEnumerable<ICard> allCards)
        {
            return 2;
        }
        #endregion
    }
}
