namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 2, true)]
    public class Chapel : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            if (!context.ActivePlayer.Hand.IsEmpty)
            {
                var trashCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(0, 4), "Select up to 4 cards to trash.");

                trashCards.MoveAll(context.ActivePlayer.Hand, context.Game.TrashZone, CardMovementVerb.Trash, context);
                foreach (var card in trashCards)
                {
                    context.Game.Log.LogTrash(context.ActivePlayer, card);
                }
            }
            else
            {
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " has no card to trash");
            }
            
        }
        #endregion
    }
}
