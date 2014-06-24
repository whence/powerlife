using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 4, true)]
    public class YoungWitch : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);

            ICard[] discardCards;
            if (context.ActivePlayer.Hand.CardCount > 2)
            {
                discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(2), "Select 2 cards to discard.");
            }
            else
            {
                discardCards = context.ActivePlayer.Hand.ToArray();
            }
            discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            context.Game.Log.LogMessage("{0} discarded {1} cards", context.ActivePlayer.Name, discardCards.Length);

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                bool baneCardRevealed = false;
                if (context.Game.Supply.BanePile != null)
                {
                    var baneCard = player.Hand.FirstOrDefault(new NameValidator<ICard>(context.Game.Supply.BanePile.Name).Validate);
                    if (baneCard != null)
                    {
                        if (context.Game.Dialog.Should(context, player, "Reveal your bane card?"))
                        {
                            context.Game.Log.LogMessage("{0} revealed the bane card {1}", player.Name, baneCard.Name);
                            baneCardRevealed = true;
                        }
                    }
                }
                
                if (!baneCardRevealed)
                {
                    var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                    if (cursePile != null)
                    {
                        if (cursePile.TopCard.MoveTo(cursePile, player.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(player, cursePile);
                    }
                }
            }
        }
        #endregion
    }
}
