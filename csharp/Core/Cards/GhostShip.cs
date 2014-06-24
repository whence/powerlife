using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class GhostShip : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;
                
                if (player.Hand.CardCount > 3)
                {
                    var putBackCards = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(player.Hand.CardCount - 3),
                        string.Format("Select {0} cards to put on top of the deck in order. Last one will be the top deck card", player.Hand.CardCount - 3));

                    putBackCards.MoveAll(player.Hand, player.Deck, CardMovementVerb.PutBackToDeck, context);
                    context.Game.Log.LogMessage("{0} put {1} cards back to deck", player.Name, putBackCards.Length);
                    Enforce.IsTrue(player.Hand.CardCount == 3);
                }
            }
        }
        #endregion
    }
}
