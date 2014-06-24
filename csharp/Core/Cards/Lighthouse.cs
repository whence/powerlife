namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Seaside, 2, true)]
    public class Lighthouse : CardBase, IActionCard, IDefenceCard
    {
        #region properties
        public bool IsDefenceOptional
        {
            get { return false; }
        }
        #endregion

        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;
            context.AvailableSpend += 1;

            context.ActivePlayer.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            context.RetainCardInPlay(this);
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.AvailableSpend += 1;
            context.Game.Log.LogMessage("Lighthouse adds one spend");
        }

        public bool IsDefenceUsable(Player player)
        {
            return (this.CurrentZone == player.PlayArea);
        }

        public bool ResolveAttack(TurnContext context, Player player)
        {
            context.Game.Log.LogMessage(player.Name + " defended the attack by Lighthouse");
            return false;
        }
        #endregion
    }
}
