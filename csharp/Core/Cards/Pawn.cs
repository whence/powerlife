namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Intrigue, 2, true)]
    public class Pawn : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            foreach (var choice in context.Game.Dialog.Choose(context, context.ActivePlayer,
                new[] { "+1 card", "+1 action", "+1 buy", "+$1", }, 2, "Choose two. Order matters"))
            {
                switch (choice)
                {
                    case 0:
                        context.ActivePlayer.DrawCards(1, context);
                        break;

                    case 1:
                        context.RemainingActions += 1;
                        break;

                    case 2:
                        context.Buys += 1;
                        break;

                    case 3:
                        context.AvailableSpend += 1;
                        break;
                }
            }
        }
        #endregion
    }
}
