using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 0, false)]
    public class Followers : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);

            var estatePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Estate>()).Validate);
            if (estatePile != null)
            {
                if (estatePile.TopCard.MoveTo(estatePile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                    context.Game.Log.LogGain(context.ActivePlayer, estatePile);
            }
            
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
        #endregion
    }
}
