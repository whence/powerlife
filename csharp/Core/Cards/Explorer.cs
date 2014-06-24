using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 5, true)]
    public class Explorer : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            IValidator<ICard> cardValidator;
            if (context.ActivePlayer.Hand.OfType<Province>().Any()
                && context.Game.Dialog.Should(context, context.ActivePlayer, "Reveal a Province to gain a Gold?"))
            {
                cardValidator = new CardTypeValidator<Gold>();
            }
            else
            {
                cardValidator = new CardTypeValidator<Silver>();
            }

            var gainPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(cardValidator).Validate);
            if (gainPile != null)
            {
                if (gainPile.TopCard.MoveTo(gainPile, context.ActivePlayer.Hand, CardMovementVerb.Gain, context))
                    context.Game.Log.LogMessage("{0} gain a {1} in hand", context.ActivePlayer.Name, gainPile.Name);
            }
        }
        #endregion
    }
}