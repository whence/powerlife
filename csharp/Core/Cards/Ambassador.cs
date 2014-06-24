using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 3, true)]
    public class Ambassador : CardBase, IActionCard 
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (context.ActivePlayer.Hand.IsEmpty)
                return;

            var revealCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                new CountValidator<ICard>(1), "Select a card to reveal").Single();

            var pile = context.Game.Supply.AllPiles.FirstOrDefault(new NameValidator<CardSupplyPile>(revealCard.Name).Validate);
            if (pile != null)
            {
                var returnCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(0, 2).AndEach(new NameValidator<ICard>(revealCard.Name)),
                    string.Format("Select up to two {0} to return to the supply", revealCard.Name));

                returnCards.MoveAll(context.ActivePlayer.Hand, pile, CardMovementVerb.ReturnToPile, context);
            }
            else
            {
                context.Game.Log.LogMessage(revealCard.Name + " is not in the supply"); // possibly a Prize card
            }

            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                if (pile != null && !pile.IsEmpty)
                {
                    if (pile.TopCard.MoveTo(pile, player.DiscardArea, CardMovementVerb.Gain, context))
                        context.Game.Log.LogGain(player, pile);
                }
            }
        }
        #endregion
    }
}
