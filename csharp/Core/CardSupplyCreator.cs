using IdeaFactory.Util;
using Powercards.Core.Cards;

namespace Powercards.Core
{
    public abstract class CardSupplyCreator
    {
        #region properties
        public int NumberOfPlayers { get; set; }
        public bool UseColonyPlatinum { get; set; }
        #endregion

        #region methods
        public CardSupply CreateSupply()
        {
            Enforce.ArgumentValid(NumberOfPlayers >= 1 && NumberOfPlayers <= 4, "NumberOfPlayers must be between 1 to 4");

            var victoryCardCount = new[] { 8, 8, 12, 12, }[NumberOfPlayers - 1];

            var supply = new CardSupply();

            supply.AddSupplyPile(typeof(Copper), null, 60 - NumberOfPlayers * 7);
            supply.AddSupplyPile(typeof(Silver), null, 40);
            supply.AddSupplyPile(typeof(Gold), null, 30);
            
            supply.AddSupplyPile(typeof(Estate), victoryCardCount, null);
            supply.AddSupplyPile(typeof(Duchy), victoryCardCount, null);
            supply.AddSupplyPile(typeof(Province), victoryCardCount, null);

            supply.AddSupplyPile(typeof(Curse), null, new[] { 10, 10, 20, 30, }[NumberOfPlayers-1]);

            AddKingdomCardPiles(supply, victoryCardCount, 10);

            if (UseColonyPlatinum)
            {
                supply.AddSupplyPile(typeof(Platinum), null, 12);
                supply.AddSupplyPile(typeof(Colony), victoryCardCount, null);
            }

            return supply;
        }

        protected abstract void AddKingdomCardPiles(CardSupply supply, int victoryCardCount, int nonVictoryCardCount);
        #endregion
    }
}
