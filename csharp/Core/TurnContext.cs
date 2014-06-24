using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class TurnContext
    {
        #region fields
        private readonly Game game;
        private readonly Player[] allPlayers;
        private readonly Player activePlayer;
        private readonly List<TurnEffect> turnEffects;
        private HashSet<string> activePlayerCannotBuyPileNames;
        private readonly bool isExtraTurn;
        private Stack<IActionCard> activePlayerActionPlayChain;
        private HashSet<ICard> cardsToRemainInPlay;
        private bool activePlayerHasBoughtVictoryCards;
        #endregion

        #region properties
        public int RemainingActions { get; set; }
        public int UnusedActions { get; private set; }
        public int PlayedActions { get; set; }
        public int AvailableSpend { get; set; }
        public int Buys { get; set; }

        public Game Game
        {
            get { return game; }
        }
        
        public Player ActivePlayer
        {
            get { return activePlayer; }
        }

        /// <remarks>
        /// If you change either Players or Opponents implementation, review their usages as there is a assumption that 
        /// player != ActivePlayer in the Players loop means Opponents loop
        /// </remarks>
        public IEnumerable<Player> Players
        {
            get { return new LoopEnumerator<Player>(this.allPlayers, Array.IndexOf(this.allPlayers, this.activePlayer), 1).AsEnumerable(); }
        }

        /// <remarks>
        /// If you change either Players or Opponents implementation, review their usages as there is a assumption that 
        /// player != ActivePlayer in the Players loop means Opponents loop
        /// </remarks>
        public IEnumerable<Player> Opponents
        {
            get { return this.Players.Skip(1); }
        }

        public TurnStage Stage { get; private set; }

        public bool IsRunning
        {
            get
            {
                switch (this.Stage)
                {
                    case TurnStage.NotStarted:
                    case TurnStage.Finished:
                        return false;

                    default:
                        return true;
                }
            }
        }

        public bool IsNextTurnExtraTurnForActivePlayer { get; private set; }
        #endregion

        #region constructors
        public TurnContext(Game game, Player[] allPlayers, Player activePlayer, bool isExtraTurn)
        {
            this.Stage = TurnStage.NotStarted;
            
            Enforce.ArgumentNotNull(game);
            this.game = game;

            Enforce.ArgumentNotNull(allPlayers);
            this.allPlayers = allPlayers;

            Enforce.ArgumentNotNull(activePlayer);
            this.activePlayer = activePlayer;

            this.isExtraTurn = isExtraTurn;
            
            this.turnEffects = new List<TurnEffect>();
            
            this.AvailableSpend = 0;
            this.RemainingActions = 1;
            this.Buys = 1;
        }
        #endregion

        #region methods
        internal void Run()
        {
            if (this.Stage != TurnStage.NotStarted)
                throw new CustomException<TurnContext>("The turn has already started");

            this.Stage = TurnStage.TurnBegun;
            
            this.Game.Log.LogTurn(this.ActivePlayer);
            this.ActivePlayer.BeginTurn(this);

            ActionLoop();
            TreasureLoop();
            BuyLoop();
            
            this.Stage = TurnStage.Cleanup;
            this.ActivePlayer.Cleanup(this);

            this.Stage = TurnStage.Finished;
        }

        private void ActionLoop()
        {
            Enforce.IsTrue(this.Stage == TurnStage.TurnBegun);

            this.Stage = TurnStage.PlayingAction;

            var cardValidator = new CardTypeValidator<IActionCard>().And(new CardZoneValidator(this.ActivePlayer.Hand));
            Func<bool> actionValidator = () => this.RemainingActions > 0 && this.Stage == TurnStage.PlayingAction;
            while (actionValidator() && this.ActivePlayer.Hand.Any(cardValidator.Validate))
            {
                var actionCards = this.Game.Dialog.Select(this, this.ActivePlayer, this.ActivePlayer.Hand,
                    new EnumerableValidator<ICard>().AndEach(cardValidator),
                    string.Format("Select action cards to play. You have {0} action cards and {1} actions left", 
                    this.ActivePlayer.Hand.Count(cardValidator.Validate), this.RemainingActions));

                if (actionCards.Length == 0)
                    break; // skip action stage

                foreach (IActionCard actionCard in actionCards)
                {
                    if (actionValidator() && cardValidator.Validate(actionCard))
                    {
                        this.RemainingActions -= 1;
                        Enforce.IsTrue(actionCard.MoveTo(this.ActivePlayer.Hand, this.ActivePlayer.PlayArea, CardMovementVerb.Play, this));

                        this.Game.Log.LogPlay(this.ActivePlayer, actionCard);
                        this.PlayedActions += 1;
                        actionCard.Play(this);
                    }
                }
            }

            this.UnusedActions = this.RemainingActions;
            this.RemainingActions = 0;
        }

        private void TreasureLoop()
        {
            Enforce.IsTrue(this.Stage == TurnStage.PlayingAction);
            
            this.Stage = TurnStage.PlayingTreasure;

            var cardValidator = new CardTypeValidator<ITreasureCard>().And(new CardZoneValidator(this.ActivePlayer.Hand));
            Func<bool> treasureValidator = () => this.Stage == TurnStage.PlayingTreasure;
            while (treasureValidator() && this.ActivePlayer.Hand.Any(cardValidator.Validate))
            {
                var treasureCards = this.Game.Dialog.Select(this, this.ActivePlayer, this.ActivePlayer.Hand,
                    new EnumerableValidator<ICard>().AndEach(cardValidator),
                    string.Format("Select treasure cards to play. {0} buys left. ${1} to spend", this.Buys, this.AvailableSpend));

                if (treasureCards.Length == 0)
                    break; // skip treasure stage

                foreach (ITreasureCard treasureCard in treasureCards)
                {
                    if (treasureValidator() && cardValidator.Validate(treasureCard))
                    {
                        Enforce.IsTrue(treasureCard.MoveTo(this.ActivePlayer.Hand, this.ActivePlayer.PlayArea, CardMovementVerb.Play, this));
                        treasureCard.PlayAndProduceValue(this);
                    }
                }
            }
        }

        private void BuyLoop()
        {
            Enforce.IsTrue(this.Stage == TurnStage.PlayingTreasure);

            this.Stage = TurnStage.Buy;

            while (this.Buys > 0)
            {
                var pilesToBuy = this.Game.Supply.AllPiles.Where(new NonEmptyPileValidator().Then(new CardCostValidator(this, this.ActivePlayer, 0, this.AvailableSpend)).Validate).ToArray();
                CardSupplyPile buyPile;
                if (pilesToBuy.Length > 0)
                {
                    buyPile = this.Game.Dialog.Select(this, this.ActivePlayer, pilesToBuy, 
                        new CountValidator<CardSupplyPile>(0, 1),
                        string.Format("Select a pile to buy. {0} buys left. ${1} to spend", this.Buys, this.AvailableSpend))
                        .SingleOrDefault();
                }
                else
                {
                    buyPile = null;
                }

                if (buyPile == null)
                    break; // skip buy stage

                var cardToBuy = buyPile.TopCard;
                var preBuyEventCard = cardToBuy as IPreBuySelfEventCard;

                bool canBuy;
                if (activePlayerCannotBuyPileNames != null && activePlayerCannotBuyPileNames.Contains(buyPile.Name))
                {
                    this.Game.Log.LogMessage("{0} cannot buy {1} this turn", this.ActivePlayer.Name, buyPile.Name);
                    canBuy = false;
                }
                else if (preBuyEventCard != null && !preBuyEventCard.MeetBuyCondition(this, this.ActivePlayer))
                {
                    this.Game.Log.LogMessage("Cannot buy {0} because condition is not met: {1}", preBuyEventCard.Name, preBuyEventCard.BuyConditionDescription);
                    canBuy = false;
                }
                else
                {
                    canBuy = true;
                }

                if (canBuy)
                {
                    if (preBuyEventCard != null)
                        preBuyEventCard.BeforeBuy(this, this.ActivePlayer);

                    this.Buys -= 1;
                    this.AvailableSpend -= cardToBuy.GetCost(this, this.ActivePlayer);

                    if (cardToBuy.MoveTo(buyPile, this.ActivePlayer.DiscardArea, CardMovementVerb.Gain, this))
                        this.Game.Log.LogBuy(this.ActivePlayer, buyPile);

                    this.activePlayerHasBoughtVictoryCards = (this.activePlayerHasBoughtVictoryCards || (cardToBuy is IVictoryCard));
                    this.ActivePlayer.AfterBuy(this, cardToBuy);
                    this.Game.AfterBuy(this, this.ActivePlayer, buyPile);
                }
            }

            this.Buys = 0;
        }

        public void AddTurnEffect(TurnEffect effect)
        {
            Enforce.IsTrue(this.IsRunning);
            
            this.turnEffects.Add(effect);
        }

        public void AddActivePlayerCannotBuyPileName(string pileName)
        {
            Enforce.IsTrue(this.IsRunning);

            if (activePlayerCannotBuyPileNames == null)
                activePlayerCannotBuyPileNames = new HashSet<string>(StringComparer.Ordinal);

            activePlayerCannotBuyPileNames.Add(pileName);
        }

        /// <returns>this will only return true for the first successful request</returns>
        public bool RequestExtraTurnForActivePlayer()
        {
            Enforce.IsTrue(this.IsRunning);

            if (this.isExtraTurn)
                return false;

            if (this.IsNextTurnExtraTurnForActivePlayer)
                return false;

            this.IsNextTurnExtraTurnForActivePlayer = true;
            return true;
        }

        public ICardZone OnCardMovement(ICard movingCard, CardMovementVerb verb, ICardZone targetZone)
        {
            Enforce.IsTrue(this.IsRunning);

            switch (verb)
            {
                case CardMovementVerb.Gain:
                    return OnCardGaining(movingCard, targetZone);

                case CardMovementVerb.Discard:
                    return OnCardDiscarding(movingCard, targetZone);

                default:
                    return targetZone;
            }
        }

        private ICardZone OnCardGaining(ICard gainingCard, ICardZone targetZone)
        {
            var gainPile = gainingCard.CurrentZone as CardSupplyPile;
            if (gainPile != null)
            {
                if (gainPile.HasTradeRouteToken)
                {
                    gainPile.MoveTradeRouteToken(this.Game);
                }
            }
            
            var playerOwnedTargetZone = targetZone as PlayerOwnedZone;
            if (playerOwnedTargetZone != null)
            {
                var player = playerOwnedTargetZone.Owner;

                player.AddGainedCardName(gainingCard.Name, this);

                List<IGainZoneAlternationCard> cardsToSelect = null;
                foreach (var card in player.Hand.Concat(player.PlayArea).OfType<IGainZoneAlternationCard>())
                {
                    if (!card.IsAlternationUsable(player))
                        continue;

                    if (cardsToSelect == null)
                        cardsToSelect = new List<IGainZoneAlternationCard>(1);

                    cardsToSelect.Add(card);
                }

                if (cardsToSelect != null)
                {
                    var selectCard = (IGainZoneAlternationCard)this.Game.Dialog.Select(this, player, cardsToSelect.ToArray(),
                        new CountValidator<ICard>(0, 1), "Select one card to use. None to skip all").SingleOrDefault();

                    if (selectCard != null)
                    {
                        targetZone = selectCard.ResolveCardZone(this, player, gainingCard, targetZone);
                    }
                }
            }
            return targetZone;
        }

        private ICardZone OnCardDiscarding(ICard discardingCard, ICardZone targetZone)
        {
            var playerOwnedTargetZone = targetZone as PlayerOwnedZone;
            if (playerOwnedTargetZone != null)
            {
                var player = playerOwnedTargetZone.Owner;

                var alternationCard = discardingCard as IDiscardSelfZoneAlternationCard;
                if (alternationCard != null)
                {
                    targetZone = alternationCard.ResolveCardZone(this, player, targetZone);
                }
            }
            return targetZone;
        }

        public bool HasBoughtVictoryCardThisTurn(Player player)
        {
            if (player != this.ActivePlayer)
                throw new NotImplementedException();

            return this.activePlayerHasBoughtVictoryCards;
        }

        public int CalculateCardCost(ICard card, int originalCost, Player player)
        {
            Enforce.IsTrue(this.IsRunning);

            var cost = originalCost;
            foreach (var effect in this.turnEffects)
            {
                if (effect.OnEvalCardCost != null)
                {
                    cost = effect.OnEvalCardCost(card, cost);
                }
            }
            foreach (var modifier in player.PlayArea.OfType<IInPlayCostModifierCard>())
            {
                cost = modifier.OnEvalCardCost(card, cost);
            }
            return cost;
        }

        public int CalculateTreasureValue(ITreasureCard card, int originalValue)
        {
            Enforce.IsTrue(this.IsRunning);

            var value = originalValue;
            foreach (var effect in this.turnEffects)
            {
                if (effect.OnEvalTreasureValue != null)
                {
                    value = effect.OnEvalTreasureValue(card, this, value);
                }
            }
            return value;
        }

        public void PushActionPlayChainForActivePlayer(IActionCard card)
        {
            Enforce.IsTrue(this.IsRunning);

            if (activePlayerActionPlayChain == null)
                activePlayerActionPlayChain = new Stack<IActionCard>(1);

            activePlayerActionPlayChain.Push(card);
        }

        public void PopActionPlayChainForActivePlayer()
        {
            Enforce.IsTrue(this.IsRunning);

            Enforce.IsTrue(activePlayerActionPlayChain != null && activePlayerActionPlayChain.Count > 0);

            // ReSharper disable PossibleNullReferenceException
            activePlayerActionPlayChain.Pop();
            // ReSharper restore PossibleNullReferenceException
        }

        public void RetainCardInPlay(ICard cardToRetain)
        {
            Enforce.IsTrue(this.IsRunning);

            if (activePlayerActionPlayChain != null)
            {
                foreach (var card in activePlayerActionPlayChain)
                {
                    AddCardToRemainInPlay(card);
                }
            }

            if (cardToRetain.BelongsTo(this.ActivePlayer))
            {
                AddCardToRemainInPlay(cardToRetain);
            }
        }

        private void AddCardToRemainInPlay(ICard card)
        {
            Enforce.IsTrue(this.IsRunning);

            if (cardsToRemainInPlay == null)
                cardsToRemainInPlay = new HashSet<ICard>();

            cardsToRemainInPlay.Add(card);
        }

        public bool ShouldRemainInPlay(ICard card)
        {
            Enforce.IsTrue(this.IsRunning);

            if (cardsToRemainInPlay == null)
                return false;

            return cardsToRemainInPlay.Contains(card);
        }

        public bool ShouldNotRemainInPlay(ICard card)
        {
            return !ShouldRemainInPlay(card);
        }
        #endregion

        #region inner classes
        public enum TurnStage
        {
            NotStarted,
            TurnBegun,
            PlayingAction,
            PlayingTreasure,
            Buy,
            Cleanup,
            Finished,
        }
        #endregion
    }
}