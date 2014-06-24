using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 7, true)]
    public class Bank : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, context.ActivePlayer.PlayArea.OfType<ITreasureCard>().Count());
        }
        #endregion
    }
}
