using System;
using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 5, true)]
    public class Saboteur : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                using (var transZone = new TransitionalZone(player))
                {
                    var cardValidator = new CardCostValidator(context, player, 3, null);
                    player.MoveFromTopDeckTill(cardValidator, 1, transZone, CardMovementVerb.Reveal, context);
                    transZone.Reveal(context.Game.Log);

                    var trashCard = transZone.SingleOrDefault(cardValidator.Validate);
                    if (trashCard != null)
                    {
                        if (trashCard.MoveTo(transZone, context.Game.TrashZone, CardMovementVerb.Trash, context))
                            context.Game.Log.LogTrash(player, trashCard);

                        var cardCost = Math.Max(trashCard.GetCost(context, player) - 2, 0);
                        var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, player, 0, cardCost)).Validate).ToArray();
                        if (pilesToGain.Length > 0)
                        {
                            var gainPile = context.Game.Dialog.Select(context, player, pilesToGain,
                                new CountValidator<CardSupplyPile>(0, 1),
                                string.Format("Select a card to gain of cost up to {0}, or none to skip", cardCost)).SingleOrDefault();

                            if (gainPile != null)
                            {
                                if (gainPile.TopCard.MoveTo(gainPile, player.DiscardArea, CardMovementVerb.Gain, context))
                                    context.Game.Log.LogGain(player, gainPile);
                            }
                        }
                        else
                        {
                            context.Game.Log.LogMessage("{0} could gain no card of appropriate cost", player.Name);
                        }
                    }
                    
                    transZone.MoveAll(transZone, player.DiscardArea, CardMovementVerb.Discard, context);
                }
            }
        }
        #endregion
    }
}
