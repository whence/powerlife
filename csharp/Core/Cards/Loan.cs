using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 3, true)]
    public class Loan : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 1);

            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                context.ActivePlayer.MoveFromTopDeckTill(new CardTypeValidator<ITreasureCard>(), 1, transZone, CardMovementVerb.Reveal, context);
                transZone.Reveal(context.Game.Log);

                transZone.Where(new InvertValidator<ICard>(new CardTypeValidator<ITreasureCard>()).Validate)
                    .MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);

                var treasureCard = (ITreasureCard)transZone.SingleOrDefault();
                if (treasureCard != null)
                {
                    switch (context.Game.Dialog.Choose(context, context.ActivePlayer, 
                        new[] { "Discard " + treasureCard.Name, "Trash " + treasureCard.Name, }, 1, "Choose one")[0])
                    {
                        case 0:
                            if (treasureCard.MoveTo(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context))
                                context.Game.Log.LogDiscard(context.ActivePlayer, treasureCard);
                            break;

                        case 1:
                            if (treasureCard.MoveTo(transZone, context.Game.TrashZone, CardMovementVerb.Trash, context))
                                context.Game.Log.LogTrash(context.ActivePlayer, treasureCard);
                            break;
                    }
                }
            }
        }
        #endregion
    }
}
