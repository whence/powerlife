using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 4, true)]
    public class Thief : CardBase, IActionCard
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
                        Enforce.IsTrue(trashCard.MoveTo(transZone, context.Game.TrashZone, CardMovementVerb.Trash, context));
                        context.Game.Log.LogTrash(player, trashCard);

                        if (context.Game.Dialog.Should(context, context.ActivePlayer,
                            string.Format("Gain {0}'s {1}", player.Name, trashCard.Name)))
                        {
                            if (trashCard.MoveTo(context.Game.TrashZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                                context.Game.Log.LogMessage("{0} gained {1}'s {2}", context.ActivePlayer.Name, player.Name, trashCard.Name);
                        }
                    }

                    transZone.MoveAll(transZone, player.DiscardArea, CardMovementVerb.Discard, context);
                }
            }
        }
        #endregion
    }
}