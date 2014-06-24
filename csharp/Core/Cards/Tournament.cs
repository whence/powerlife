using System.Linq;

namespace Powercards.Core.Cards
{
    [CardInfo(CardSet.Cornucopia, 4, true)]
    public class Tournament : CardBase, IActionCard
    {
        #region methods
        public void Play(TurnContext context)
        {
            context.RemainingActions += 1;

            var numberOfProvincesRevealedByOthers = 0;
            foreach (var player in context.Players)
            {
                var province = player.Hand.FirstOrDefault(new CardTypeValidator<Province>().Validate);
                if (province != null)
                {
                    if (context.Game.Dialog.Should(context, player, "Reveal your province?"))
                    {
                        context.Game.Log.LogMessage(player.Name + " revealed a Province");

                        if (player == context.ActivePlayer)
                        {
                            if (province.MoveTo(player.Hand, player.DiscardArea, CardMovementVerb.Discard, context))
                                context.Game.Log.LogDiscard(player, province);

                            switch (context.Game.Dialog.Choose(context, player, 
                                new[] { "Gain a Prize", "Gain a Duchy", }, 1, "Choose one to gain and put on top deck")[0])
                            {
                                case 0:
                                    if (!context.Game.PrizeZone.IsEmpty)
                                    {
                                        var gainCard = context.Game.Dialog.Select(context, player, context.Game.PrizeZone,
                                            new CountValidator<ICard>(1), "Select a Prize to gain").Single();

                                        if (gainCard.MoveTo(context.Game.PrizeZone, player.Deck, CardMovementVerb.Gain, context))
                                            context.Game.Log.LogGain(player, gainCard);
                                    }
                                    else
                                    {
                                        context.Game.Log.LogMessage("There is no more Prize card to gain");
                                    }
                                    break;

                                case 1:
                                    var duchyPile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Duchy>()).Validate);
                                    if (duchyPile != null)
                                    {
                                        if (duchyPile.TopCard.MoveTo(duchyPile, player.Deck, CardMovementVerb.Gain, context))
                                            context.Game.Log.LogGain(player, duchyPile);
                                    }
                                    break;
                            }
                        }
                        else
                        {
                            numberOfProvincesRevealedByOthers++;
                        }
                    }
                }
            }

            if (numberOfProvincesRevealedByOthers == 0)
            {
                // ReSharper disable PossibleNullReferenceException
                context.ActivePlayer.DrawCards(1, context);
                // ReSharper restore PossibleNullReferenceException
                context.AvailableSpend += 1;
            }
        }
        #endregion
    }
}
