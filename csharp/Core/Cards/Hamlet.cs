using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 2, true)]
    public class Hamlet : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;

            for (int i = 0; i < 2; i++)
            {
                if (!context.ActivePlayer.Hand.IsEmpty)
                {
                    var description = string.Empty;
                    switch (i)
                    {
                        case 0:
                            description = "You may discard a card for +1 action";
                            break;

                        case 1:
                            description = "You may discard a card for +1 buy";
                            break;
                    }
                    
                    var discardCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                        new CountValidator<ICard>(0, 1), description).SingleOrDefault();

                    if (discardCard != null)
                    {
                        if (discardCard.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context))
                            context.Game.Log.LogDiscard(context.ActivePlayer, discardCard);

                        switch (i)
                        {
                            case 0:
                                context.RemainingActions += 1;
                                break;

                            case 1:
                                context.Buys += 1;
                                break;
                        }
                    }
                }
            }
        }
        #endregion
    }
}
