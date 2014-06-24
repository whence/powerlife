using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public static class Toolbox
    {
        public static GenericEnumerable<T> AsEnumerable<T>(this IEnumerator<T> enumerator)
        {
            return new GenericEnumerable<T>(enumerator);
        }

        public static IEnumerable<T> DistinctByName<T>(this IEnumerable<T> items)
            where T : INameIdentifiable
        {
            return items.GroupBy(x => x.Name, (x, y) => y.First(), StringComparer.Ordinal);
        }

        public static void MoveAll(this IEnumerable<ICard> cards, ICardZone sourceZone, ICardZone targetZone, CardMovementVerb verb, TurnContext context)
        {
            MoveAll(cards.ToArray(), sourceZone, targetZone, verb, context);
            Enforce.IsFalse(cards.Any());
        }

        public static void MoveAll(this ICard[] cards, ICardZone sourceZone, ICardZone targetZone, CardMovementVerb verb, TurnContext context)
        {
            foreach (var card in cards)
            {
                card.MoveTo(sourceZone, targetZone, verb, context);
            }
        }

        public static int RoundDown(int x, int y)
        {
            return (int)Math.Floor((float)x/y);
        }

        public static bool BelongsTo(this ICard card, Player player)
        {
            var playerOwnedZone = card.CurrentZone as PlayerOwnedZone;
            if (playerOwnedZone == null)
                return false;

            return playerOwnedZone.Owner == player;
        }
    }
}
