using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class Swindler : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;
            
            foreach (var player in context.Opponents)
            {
                if (!player.OnAttack(context))
                    continue;

                var trashedCard = player.MoveOneFromTopDeck(context.Game.TrashZone, CardMovementVerb.Trash, context);
                if (trashedCard != null)
                {
                    context.Game.Log.LogTrash(player, trashedCard);

                    var cardCost = trashedCard.GetCost(context, player);
                    var pilesToGain = context.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(context, context.ActivePlayer, cardCost)).Validate).ToArray();
                    if (pilesToGain.Length > 0)
                    {
                        var gainPile = context.Game.Dialog.Select(context, context.ActivePlayer, pilesToGain, 
                            new CountValidator<CardSupplyPile>(1),
                            string.Format("Select a card for {0} to gain of cost {1}.", player.Name, cardCost)).Single();

                        if (gainPile.TopCard.MoveTo(gainPile, player.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(player, gainPile);
                    }
                    else
                    {
                        context.Game.Log.LogMessage(player.Name + " could gain no card of appropriate cost");
                    }
                }
                else
                {
                    context.Game.Log.LogMessage(player.Name + " has no card in deck to trash");
                }
            }
        }
        #endregion
    }
}