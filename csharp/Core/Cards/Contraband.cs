namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Contraband : CardBase, ITreasureCard
    {
        #region methods
        public void PlayAndProduceValue(TurnContext context)
        {
            context.AvailableSpend += context.CalculateTreasureValue(this, 3);
            context.Buys += 1;

            var cannotBuyPileName = context.Game.Dialog.Name(context, context.Game.NextPlayerOf(context.ActivePlayer),
                string.Format("Select a pile that {0} cannot buy this turn. {0} currently has ${1}, {2} buys and {3} cards in hand",
                context.ActivePlayer.Name, context.AvailableSpend, context.Buys, context.ActivePlayer.Hand.CardCount));

            context.AddActivePlayerCannotBuyPileName(cannotBuyPileName);
        }
        #endregion
    }
}
