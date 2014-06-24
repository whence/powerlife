using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Rabble : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(3, context);
            
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                using (var transZone = new TransitionalZone(player))
                {
                    player.MoveFromTopDeck(3, transZone, CardMovementVerb.Reveal, context);
                    transZone.Reveal(context.Game.Log);

                    transZone.Where(new CardTypeValidator<IActionCard, ITreasureCard>().Validate)
                        .MoveAll(transZone, player.DiscardArea, CardMovementVerb.Discard, context);

                    ICard[] putBackCards;
                    switch (transZone.CardCount)
                    {
                        case 0:
                        case 1:
                            putBackCards = transZone.ToArray();
                            break;

                        default:
                            putBackCards = context.Game.Dialog.Select(context, player, transZone,
                                new CountValidator<ICard>(transZone.CardCount),
                                "Select these cards to put on top of the deck in order. Last one will be the top deck card");
                            break;
                    }

                    putBackCards.MoveAll(transZone, player.Deck, CardMovementVerb.PutBackToDeck, context);
                    foreach (var card in putBackCards)
                    {
                        context.Game.Log.LogPutBack(player, card);
                    }
                }
            }
        }
        #endregion
    }
}