using System.Collections.Generic;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 2, true)]
    public class SecretChamber : CardBase, IActionCard, IDefenceCard
    {
        #region properties
        public bool IsDefenceOptional
        {
            get { return true; }
        }
        #endregion
        
        #region methods
        public void Play(TurnContext context)
        {
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new NullValidator<IEnumerable<ICard>>(), "Select any number of cards to discard, you will gain $1 per discard");

                if (discardCards.Length > 0)
                {
                    discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
                    context.Game.Log.LogMessage("{0} discarded {1} cards", context.ActivePlayer.Name, discardCards.Length);
                    context.AvailableSpend += discardCards.Length;
                }
            }
        }

        public bool IsDefenceUsable(Player player)
        {
            return (this.CurrentZone == player.Hand);
        }

        public bool ResolveAttack(TurnContext context, Player player)
        {
            context.Game.Log.LogMessage(player.Name + " react by using Secret Chamber");
            
            player.DrawCards(2, context);
            context.Game.Log.LogMessage(player.Name + " drawed 2 cards");

            ICard[] putBackCards;
            switch (player.Hand.CardCount)
            {
                case 0:
                case 1:
                    putBackCards = player.Hand.ToArray();
                    break;

                default:
                    putBackCards = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(2),
                        "Select 2 cards to put on top of the deck in order. Last one will be the top deck card");
                    break;
            }
            putBackCards.MoveAll(player.Hand, player.Deck, CardMovementVerb.PutBackToDeck, context);
            context.Game.Log.LogMessage("{0} put back {1} cards", player.Name, putBackCards.Length);

            return true;
        }
        #endregion
    }
}