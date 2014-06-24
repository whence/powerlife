using Powercards.Core.Cards;
using Powercards.Core;

namespace Powercards.Wincli
{
    public class FixedCardSupplyCreator : CardSupplyCreator
    {
        protected override void AddKingdomCardPiles(CardSupply supply, int victoryCardCount, int nonVictoryCardCount)
        {
            supply.AddSupplyPile(typeof(Monument), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Library), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Ambassador), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Caravan), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Ironworks), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Menagerie), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Saboteur), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Baron), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Bridge), victoryCardCount, nonVictoryCardCount);
            supply.AddSupplyPile(typeof(Bishop), victoryCardCount, nonVictoryCardCount);
        }
    }
}
