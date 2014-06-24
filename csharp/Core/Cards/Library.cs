namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Base, 5, true)]
    public class Library : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            // this is not exactly as manual says. 
            // all cards goes through transtionalZone first before going to hand or reveal then discard, 
            // whereas the manual says cards should go to hand or setside then discard
            using (var transZone = new TransitionalZone(context.ActivePlayer))
            {
                while (context.ActivePlayer.Hand.CardCount < 7)
                {
                    var setAsidedCard = context.ActivePlayer.MoveOneFromTopDeck(transZone, CardMovementVerb.SetAside, context);
                    if (setAsidedCard == null)
                        break;

                    bool shouldMoveToHand;
                    if (new CardTypeValidator<IActionCard>().Validate(setAsidedCard))
                    {
                        if (context.Game.Dialog.Should(context, context.ActivePlayer, "Set aside " + setAsidedCard.Name + "?"))
                        {
                            shouldMoveToHand = false;
                        }
                        else
                        {
                            shouldMoveToHand = true;
                        }
                    }
                    else
                    {
                        shouldMoveToHand = true;
                    }

                    if (shouldMoveToHand)
                    {
                        setAsidedCard.MoveTo(transZone, context.ActivePlayer.Hand, CardMovementVerb.Draw, context);
                    }
                }
                transZone.Reveal(context.Game.Log);
                transZone.MoveAll(transZone, context.ActivePlayer.DiscardArea, CardMovementVerb.Discard, context);
            }
        }
        #endregion
    }
}
