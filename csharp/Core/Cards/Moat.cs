namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 2, true)]
    public class Moat : CardBase, IActionCard, IDefenceCard
    {
        #region properties
        public bool IsDefenceOptional
        {
            get { return true; }
        }
        #endregion
        
        #region methods
        public void Play(TurnContext context)
        {
            context.ActivePlayer.DrawCards(2, context);
        }

        public bool IsDefenceUsable(Player player)
        {
            return (this.CurrentZone == player.Hand);
        }

        public bool ResolveAttack(TurnContext context, Player player)
        {
            context.Game.Log.LogMessage(player.Name + " defended the attack by Moat");
            return false;
        }
        #endregion
    }
}