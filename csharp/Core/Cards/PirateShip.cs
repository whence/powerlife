using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 4, true)]
    public class PirateShip : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            switch (context.Game.Dialog.Choose(context, context.ActivePlayer,
                new[] { "Collect coins", "Use coins", }, 1, "Choose one")[0])
            {
                case 0:
                    var trashedCardCount = 0;
                    foreach (var player in context.Opponents)
                    {
                        if (!player.OnAttack(context))
                            continue;

                        using (var transZone = new TransitionalZone(player))
                        {
                            player.MoveFromTopDeck(2, transZone, CardMovementVerb.Reveal, context);
                            transZone.Reveal(context.Game.Log);

                            var uniqueTreasureCards = transZone.OfType<ITreasureCard>().DistinctByName().ToArray();
                            ICard trashCard;
                            switch (uniqueTreasureCards.Length)
                            {
                                case 0:
                                    trashCard = null;
                                    break;

                                case 1:
                                    trashCard = uniqueTreasureCards[0];
                                    break;

                                default:
                                    trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, uniqueTreasureCards,
                                        new CountValidator<ICard>(1), "Select a card to trash").Single();
                                    break;
                            }
                            if (trashCard != null)
                            {
                                if (trashCard.MoveTo(transZone, context.Game.TrashZone, CardMovementVerb.Trash, context))
                                    context.Game.Log.LogTrash(player, trashCard);

                                trashedCardCount++;
                            }

                            transZone.MoveAll(transZone, player.DiscardArea, CardMovementVerb.Discard, context);
                        }
                    }

                    if (trashedCardCount > 0)
                    {
                        context.ActivePlayer.CoinTokens += 1;
                    }
                    break;

                case 1:
                    foreach (var player in context.Opponents)
                    {
                        player.OnAttack(context);
                    }

                    context.AvailableSpend += context.ActivePlayer.CoinTokens;
                    break;
            }
        }
        #endregion
    }
}
