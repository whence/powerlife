using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 6, true)]
    public class GrandMarket : CardBase, IActionCard, IPreBuySelfEventCard
    {
        #region properties
        public string BuyConditionDescription
        {
            get { return "Have no Coppers in play"; }
        }
        #endregion

        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 1;
            context.Buys += 1;
            context.AvailableSpend += 2;
        }

        public bool MeetBuyCondition(TurnContext context, Player player)
        {
            return !player.PlayArea.OfType<Copper>().Any();
        }

        public void BeforeBuy(TurnContext context, Player player)
        {
        }
        #endregion
    }
}
