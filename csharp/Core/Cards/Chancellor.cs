namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 3, true)]
    public class Chancellor : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.AvailableSpend += 2;

            if (context.Game.Dialog.Should(context, context.ActivePlayer, "Put your deck into your discard pile?"))
            {
                context.ActivePlayer.DiscardDeck(context);
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " put his deck in his discard pile");
            }
        }
        #endregion
    }
}