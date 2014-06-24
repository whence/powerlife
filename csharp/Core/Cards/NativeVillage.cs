namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 2, true)]
    public class NativeVillage : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 2;

            switch (context.Game.Dialog.Choose(context, context.ActivePlayer,
                new[] { "Set aside top card of deck to Native Village mat", "Put all cards from Native Village mat to hand", }, 1, "Choose one")[0])
            {
                case 0:
                    if (context.ActivePlayer.MoveOneFromTopDeck(context.ActivePlayer.NativeVillageMat, CardMovementVerb.SetAside, context) != null)
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " set aside a card to Native Village mat");
                    break;

                case 1:
                    if (!context.ActivePlayer.NativeVillageMat.IsEmpty)
                    {
                        context.ActivePlayer.NativeVillageMat.MoveAll(context.ActivePlayer.NativeVillageMat, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context);
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " put all cards from Native Village mat into hand");
                    }
                    break;
            }
        }
        #endregion
    }
}
