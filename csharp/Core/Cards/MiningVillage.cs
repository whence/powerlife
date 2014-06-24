namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 4, true)]
    public class MiningVillage : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.RemainingActions += 2;

            if (this.CurrentZone != context.Game.TrashZone
                && context.Game.Dialog.Should(context, context.ActivePlayer, "Trash mining village for $2?"))
            {
                if (this.MoveTo(context.ActivePlayer.PlayArea, context.Game.TrashZone, CardMovementVerb.Trash, context))
                    context.Game.Log.LogTrash(context.ActivePlayer, this);

                context.AvailableSpend += 2;
            }
        }
        #endregion
    }
}
