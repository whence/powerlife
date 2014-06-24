using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 6, true)]
    public class Goons : CardBase, IActionCard, IPostBuyOtherCardEventCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.Buys += 1;
            context.AvailableSpend += 2;

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                if (player.Hand.CardCount > 3)
                {
                    var discardCards = context.Game.Dialog.Select(context, player, player.Hand,
                        new CountValidator<ICard>(player.Hand.CardCount - 3),
                        string.Format("Select {0} cards to discard", player.Hand.CardCount - 3));

                    discardCards.MoveAll(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context);
                    foreach (var card in discardCards)
                    {
                        context.Game.Log.LogDiscard(player, card);
                    }
                }
                Enforce.IsTrue(player.Hand.CardCount == 3);
            }
        }

        public void AfterBuy(TurnContext context, Player player, ICard card)
        {
            player.VPTokens += 1;
        }
        #endregion
    }
}
