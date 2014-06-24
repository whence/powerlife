using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 2, true)]
    public class Haven : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;

            var durationEffect = new AttachmentDurationEffect { IsAttachedCardHidden = true,};
            
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var setAsideCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(1), "Select a card to set aside").Single();

                if (setAsideCard.MoveTo(context.ActivePlayer.Hand, context.ActivePlayer.SetAsideArea, CardMovementVerb.SetAside, context))
                    context.Game.Log.LogMessage(context.ActivePlayer.Name + " set aside a card to Haven");
                
                durationEffect.AttachedCard = setAsideCard;
            }
            else
            {
                context.Game.Log.LogMessage("{0} did not have any cards to set aside to Haven.", context.ActivePlayer.Name);
            }

            // we add duration effect regardlessly and ensure haven card remains in play
            context.ActivePlayer.AddDurationEffect(durationEffect);
            context.RetainCardInPlay(this);
        }
        #endregion
    }
}
