using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Prosperity, 5, true)]
    public class Mint : CardBase, IActionCard, IPreBuySelfEventCard
    {
        #region properties
        public string BuyConditionDescription
        {
            get { return string.Empty; }
        }
        #endregion

        #region methods
        public void Play(TurnContext context)
        {
            if (!context.ActivePlayer.Hand.OfType<ITreasureCard>().Any())
            {
                var copyCard = context.Game.Dialog.Select(context, context.ActivePlayer, context.ActivePlayer.Hand,
                    new CountValidator<ICard>(0, 1).AndEach(new CardTypeValidator<ITreasureCard>()), 
                    "Select a treasure card to copy").SingleOrDefault();

                if (copyCard != null)
                {
                    var pile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new NameValidator<ICard>(copyCard.Name)).Validate);
                    if (pile != null)
                    {
                        if (pile.TopCard.MoveTo(pile, context.ActivePlayer.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(context.ActivePlayer, pile);
                    }
                }
            }
            else
            {
                context.Game.Log.LogMessage(context.ActivePlayer.Name + " did not have any treasure cards to Mint");
            }
        }

        public bool MeetBuyCondition(TurnContext context, Player player)
        {
            return true;
        }

        public void BeforeBuy(TurnContext context, Player player)
        {
            player.PlayArea.OfType<ITreasureCard>().MoveAll(player.PlayArea, context.Game.TrashZone, CardMovementVerb.Trash, context);
            context.Game.Log.LogMessage(player.Name + " trashed all treasure cards in play");
        }
        #endregion
    }
}
