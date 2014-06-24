using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class Torturer : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(3, context);

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                switch (context.Game.Dialog.Choose(context, player, new[] { "Discard two cards", "Gain a curse", }, 1, "Choose one")[0])
                {
                    case 0:
                        ICard[] discardCards;
                        if (player.Hand.CardCount > 2)
                        {
                            discardCards = context.Game.Dialog.Select(context, player, player.Hand,
                                new CountValidator<ICard>(2), "Select 2 cards to discard.");
                        }
                        else
                        {
                            discardCards = player.Hand.ToArray();
                        }
                        discardCards.MoveAll(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context);
                        
                        if (discardCards.Length > 0)
                        {
                            foreach (var card in discardCards)
                            {
                                context.Game.Log.LogDiscard(player, card);
                            }
                        }
                        else
                        {
                            context.Game.Log.LogMessage(player.Name + " has no card to discard");
                        }
                        break;

                    case 1:
                        var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                        if (cursePile != null)
                        {
                            if (cursePile.TopCard.MoveTo(cursePile, player.Hand, CardMovementVerb.Gain, context))
                                context.Game.Log.LogGain(player, cursePile);
                        }
                        break;
                }
            }
        }
        #endregion
    }
}