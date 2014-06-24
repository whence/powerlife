using System;
using System.Collections.Generic;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class CardSupply
    {
        #region fields
        private readonly List<CardSupplyPile> allPiles;
        #endregion

        #region properties
        public IEnumerable<CardSupplyPile> AllPiles
        {
            get { return allPiles; }
        }

        public CardSupplyPile BanePile { get; private set; }
        #endregion

        #region constructors
        internal CardSupply()
        {
            this.allPiles = new List<CardSupplyPile>();
        }
        #endregion

        #region methods
        public void AddSupplyPile(Type cardType, int? victoryCardCount, int? nonVictoryCardCount, bool isBanePile = false)
        {
            var isVictoryCard = typeof(IVictoryCard).IsAssignableFrom(cardType);
            var isGameEndingPile = typeof(IGameEndingCard).IsAssignableFrom(cardType);

            var pile = new CardSupplyPile(CardCreator.GenerateCardName(cardType), isGameEndingPile, isVictoryCard);
            var cardCount = isVictoryCard ? victoryCardCount : nonVictoryCardCount;
            Enforce.IsTrue(cardCount.HasValue);

            for (int i = 0; i < cardCount.GetValueOrDefault(); i++)
            {
                CardCreator.Create(cardType, pile);
            }
            allPiles.Add(pile);

            if (isBanePile)
            {
                this.BanePile = pile;
            }
        }

        public bool ShouldEndGame()
        {
            var emptyPileCount = 0;
            foreach (var pile in allPiles)
            {
                if (pile.IsEmpty)
                {
                    if (pile.IsGameEndingPile)
                        return true;
                    
                    emptyPileCount++;
                }
            }
            return emptyPileCount >= 3;
        }
        #endregion
    }
}