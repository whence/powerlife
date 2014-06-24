using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;
using Powercards.Core.Cards;

namespace Powercards.Core
{
    public class RandomCardSupplyCreator : CardSupplyCreator
    {
        #region methods
        protected override void AddKingdomCardPiles(CardSupply supply, int victoryCardCount, int nonVictoryCardCount)
        {
            var cardInterface = typeof(ICard);
            var list = new List<Tuple<Type, CardInfoAttribute>>();
            foreach (var cardType in typeof(Copper).Assembly.GetTypes())
            {
                if (cardInterface.IsAssignableFrom(cardType) && !cardType.IsAbstract)
                {
                    var cardInfo = (CardInfoAttribute)cardType.GetCustomAttributes(typeof(CardInfoAttribute), false).Single();
                    if (cardInfo.IsKingdomCard)
                    {
                        list.Add(new Tuple<Type, CardInfoAttribute>(cardType, cardInfo));
                    }
                }
            }
            var array = list.ToArray();
            var random = new Random(Maths.RandomInt32());
            for (int i = 0; i < 5; i++)
            {
                CollectionUtil.Shuffle(array, random);
            }

            const int standardKingdomPileCount = 10;
            Enforce.IsTrue(array.Length >= standardKingdomPileCount);
            var selected = array.Take(standardKingdomPileCount).OrderBy(x => x.Item2.OriginalCost).ThenBy(x => x.Item1.Name);
            foreach (var pair in selected)
            {
                supply.AddSupplyPile(pair.Item1, victoryCardCount, nonVictoryCardCount);
            }

            var prosperityPileCount = selected.Count(x => x.Item2.CardSet == CardSet.Prosperity);
            this.UseColonyPlatinum = (random.Next(standardKingdomPileCount) < prosperityPileCount);

            if (selected.Any(x => x.Item1 == typeof(YoungWitch)))
            {
                supply.AddSupplyPile(array.Skip(standardKingdomPileCount).First(x => x.Item2.OriginalCost == 2 || x.Item2.OriginalCost == 3).Item1,
                    victoryCardCount, nonVictoryCardCount, true);
            }
        }
        #endregion
    }
}
