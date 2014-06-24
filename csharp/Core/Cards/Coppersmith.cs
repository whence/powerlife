namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class Coppersmith : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AddTurnEffect(new TurnEffect { OnEvalTreasureValue = OnEvalTreasureValue, });
        }

        private static int OnEvalTreasureValue(ITreasureCard card, TurnContext context, int value)
        {
            return card is Copper ? value + 1 : value;
        }
        #endregion
    }
}
