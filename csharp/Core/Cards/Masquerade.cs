using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 3, true)]
    public class Masquerade : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);

            // note: this is NOT attack, according to manual
            var movements = new List<Tuple<Player, ICard>>();
            using (var transZone = new SharedTransitionalZone())
            {
                foreach (var player in context.Players)
                {
                    if (!player.Hand.IsEmpty)
                    {
                        var passCard = context.Game.Dialog.Select(context, player, player.Hand,
                            new CountValidator<ICard>(1), "Select a card to pass").Single();

                        Enforce.IsTrue(passCard.MoveTo(player.Hand, transZone, CardMovementVerb.PlaceFaceDown, context));
                        movements.Add(new Tuple<Player, ICard>(player, passCard));
                    }
                }
                foreach (var movement in movements)
                {
                    var player = movement.Item1;
                    var passCard = movement.Item2;
                    var nextPlayer = context.Game.NextPlayerOf(player);
                    Enforce.IsTrue(passCard.MoveTo(transZone, nextPlayer.Hand, CardMovementVerb.Pass, context));
                    context.Game.Log.LogMessage("{0} passed a card to {1}", player.Name, nextPlayer.Name);
                }
            }

            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(0, 1), "Select up to 1 card to trash").SingleOrDefault();

                if (trashCard != null)
                {
                    if (trashCard.MoveTo(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context))
                        context.Game.Log.LogTrash(context.ActivePlayer, trashCard);
                }
            }
        }
        #endregion
    }
}