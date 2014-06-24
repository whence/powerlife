using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 4, true)]
    public class HorseTraders : CardBase, IActionCard, IDefenceCard
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
            context.Buys += 1;
            context.AvailableSpend += 3;

            ICard[] discardCards;
            if (context.ActivePlayer.Hand.CardCount > 2)
            {
                discardCards = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(2), "Select 2 cards to discard.");
            }
            else
            {
                discardCards = context.ActivePlayer.Hand.ToArray();
            }
            discardCards.MoveAll(context.ActivePlayer.Hand, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            context.Game.Log.LogMessage("{0} discarded {1} cards", context.ActivePlayer.Name, discardCards.Length);
        }

        public bool IsDefenceUsable(Player player)
        {
            return (this.CurrentZone == player.Hand);
        }

        public bool ResolveAttack(TurnContext context, Player player)
        {
            context.Game.Log.LogMessage(player.Name + " react by using Horse Traders");

            Enforce.IsTrue(this.MoveTo(player.Hand, player.SetAsideArea, CardMovementVerb.SetAside, context));
            context.Game.Log.LogMessage(player.Name + " set aside a Horse Traders");

            player.AddDurationEffect(new SimpleDurationEffect(OnTurnStarting));
            player.AddDurationEffect(new AttachmentDurationEffect { AttachedCard = this, });

            return true;
        }

        private static void OnTurnStarting(TurnContext context)
        {
            context.ActivePlayer.DrawCards(1, context);
            context.Game.Log.LogMessage("Horse Traders draws an extra card");
        }
        #endregion
    }
}
