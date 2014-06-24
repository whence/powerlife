using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 0, false)]
    public class TrustySteed : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            foreach (var choice in context.Game.Dialog.Choose(context, context.ActivePlayer,
                new[] { "+2 card", "+2 action", "+$2", "Gain 4 silvers and discard deck" }, 2, 
                "Choose two. Order does not matter.").OrderBy(x=>x))
            {
                switch (choice)
                {
                    case 0:
                        context.ActivePlayer.DrawCards(2, context);
                        break;

                    case 1:
                        context.RemainingActions += 2;
                        break;

                    case 2:
                        context.AvailableSpend += 2;
                        break;

                    case 3:
                        var silverPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Silver>()).Validate);
                        for (int i = 0; i < 4; i++)
                        {
                            if (silverPile != null && !silverPile.IsEmpty)
                            {
                                if (silverPile.TopCard.MoveTo(silverPile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                                    context.Game.Log.LogGain(context.ActivePlayer, silverPile);
                            }
                        }

                        context.ActivePlayer.DiscardDeck(context);
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " put his deck in his discard pile");
                        break;
                }
            }
        }
        #endregion
    }
}
