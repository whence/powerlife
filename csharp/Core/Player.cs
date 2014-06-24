using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;
using Powercards.Core.Cards;

namespace Powercards.Core
{
    public class Player
    {
        #region fields
        private readonly string name;
        private readonly Random random;
        private readonly Deck deck;
        private readonly PlayerOwnedZone discardArea;
        private readonly PlayerOwnedZone hand;
        private readonly PlayerOwnedZone playArea;
        private readonly PlayerOwnedZone setAsideArea;
        private readonly PlayerOwnedZone nativeVillageMat;
        private readonly List<IDurationEffect> durationEffects;
        private readonly HashSet<string> recentGainedCardNamesOnMyTurn;
        #endregion

        #region properties
        public string Name
        {
            get { return name; }
        }

        public ICardZone Deck
        {
            get { return deck; }
        }

        public int DeckCardCount
        {
            get { return deck.CardCount; }
        }

        public PlayerOwnedZone DiscardArea
        {
            get { return discardArea; }
        }

        public PlayerOwnedZone Hand
        {
            get { return hand; }
        }

        public PlayerOwnedZone PlayArea
        {
            get { return playArea; }
        }

        /// <summary>
        /// Short-term set aside should use TransitionalZone, this is for long-term set aside such as Haven, Island, etc
        /// </summary>
        public PlayerOwnedZone SetAsideArea
        {
            get { return setAsideArea; }
        }

        public PlayerOwnedZone NativeVillageMat
        {
            get { return nativeVillageMat; }
        }

        public int TurnCount { get; private set; }

        public int CoinTokens { get; set; }
        public int VPTokens { get; set; }
        #endregion

        #region constructors
        public Player(string name)
        {
            Enforce.ArgumentNotEmptyOrNull(name);
            this.name = name;

            this.random = new Random(Maths.RandomInt32());
            this.discardArea = new PlayerOwnedZone(this);
            this.deck = new Deck(this);
            this.hand = new PlayerOwnedZone(this);
            this.playArea = new PlayerOwnedZone(this);
            this.setAsideArea = new PlayerOwnedZone(this);
            this.nativeVillageMat = new PlayerOwnedZone(this);
            this.durationEffects = new List<IDurationEffect>();
            this.recentGainedCardNamesOnMyTurn = new HashSet<string>(StringComparer.Ordinal);
        }
        #endregion

        #region methods
        public void Init()
        {
            InitDeckAndHand();
        }

        private void InitDeckAndHand()
        {
            var cardTypes = new Type[10];
            var index = 0;
            for (int i = 0; i < 3; i++)
            {
                cardTypes[index++] = typeof(Estate);
            }
            for (int i = 0; i < 7; i++)
            {
                cardTypes[index++] = typeof(Copper);
            }
            Enforce.IsTrue(index == cardTypes.Length);

            Shuffle(cardTypes);

            index = 0;
            for (int i = 0; i < 5; i++)
            {
                CardCreator.Create(cardTypes[index++], this.Deck);
            }
            for (int i = 0; i < 5; i++)
            {
                CardCreator.Create(cardTypes[index++], this.Hand);
            }
            Enforce.IsTrue(index == cardTypes.Length);
        }

        public void BeginTurn(TurnContext context)
        {
            this.TurnCount++;

            this.recentGainedCardNamesOnMyTurn.Clear();

            foreach (var effect in this.durationEffects)
            {
                effect.OnTurnStarting(context);
            }
            this.durationEffects.Clear();
        }

        public void DrawCards(int numberOfCards, TurnContext context)
        {
            MoveFromTopDeck(numberOfCards, this.Hand, CardMovementVerb.Draw, context);
        }

        public void DrawCardsTill(int handCardCountUpTo, TurnContext context)
        {
            MoveFromTopDeckTill(new NullValidator<ICard>(), handCardCountUpTo, this.Hand, CardMovementVerb.Draw, context);
        }

        public ICard MoveOneFromTopDeck(ICardZone zone, CardMovementVerb verb, TurnContext context)
        {
            ICard movedCard;
            TryMoveFromDeckImpl(true, zone, verb, context, out movedCard);
            return movedCard;
        }

        public void MoveFromTopDeck(int numberOfCards, ICardZone targetZone, CardMovementVerb verb, TurnContext context)
        {
            for (int i = 0; i < numberOfCards; i++)
            {
                ICard movedCard;
                if (!TryMoveFromDeckImpl(true, targetZone, verb, context, out movedCard))
                    break;
            }
        }

        public void MoveFromBottomDeck(int numberOfCards, ICardZone targetZone, CardMovementVerb verb, TurnContext context)
        {
            for (int i = 0; i < numberOfCards; i++)
            {
                ICard movedCard;
                if (!TryMoveFromDeckImpl(false, targetZone, verb, context, out movedCard))
                    break;
            }
        }

        public void MoveFromTopDeckTill<TZone>(IValidator<ICard> validator, int validatedCountUpTo, TZone targetZone, CardMovementVerb verb, TurnContext context)
            where TZone : ICardZone, IEnumerable<ICard>
        {
            while (targetZone.Count(validator.Validate) < validatedCountUpTo)
            {
                ICard movedCard;
                if (!TryMoveFromDeckImpl(true, targetZone, verb, context, out movedCard))
                    break;
            }
        }

        /// <returns>return false if the move failed. It also means that the deck cannot be drawn anymore</returns>
        private bool TryMoveFromDeckImpl(bool trueForTopCardFalseForButtom, ICardZone targetZone, CardMovementVerb verb, TurnContext context, out ICard movedCard)
        {
            RecycleDeckIfEmpty(context);
            
            if (this.deck.IsEmpty)
            {
                movedCard = null;
                return false;
            }

            movedCard = (trueForTopCardFalseForButtom ? this.deck.TopCard : this.deck.BottomCard);
            movedCard.MoveTo(this.Deck, targetZone, verb, context);
            return true;
        }

        private void RecycleDeckIfEmpty(TurnContext context)
        {
            if (this.deck.IsEmpty)
            {
                if (!this.DiscardArea.IsEmpty)
                {
                    var discardCards = this.DiscardArea.ToArray();
                    Shuffle(discardCards);
                    discardCards.MoveAll(this.DiscardArea, this.Deck, CardMovementVerb.Shuffle, context);
                    Enforce.IsTrue(this.DiscardArea.IsEmpty);
                }
            }
        }

        public void DiscardDeck(TurnContext context)
        {
            this.deck.MoveAll(this.Deck, this.DiscardArea, CardMovementVerb.Discard, context);
        }

        /// <returns>Can continue the attack?</returns>
        public bool OnAttack(TurnContext context)
        {
            var continueAttack = true;

            HashSet<IDefenceCard> resolvedCards = null;
            while (true)
            {
                List<IDefenceCard> cardsToSelect = null;
                IDefenceCard cardToResolve = null;
                foreach (var card in this.Hand.Concat(this.PlayArea).OfType<IDefenceCard>())
                {
                    if (!card.IsDefenceUsable(this))
                        continue;
                    
                    if (resolvedCards == null)
                        resolvedCards = new HashSet<IDefenceCard>();

                    if (resolvedCards.Contains(card))
                        continue;

                    if (!card.IsDefenceOptional)
                    {
                        cardToResolve = card;
                        break;
                    }

                    if (cardsToSelect == null)
                        cardsToSelect = new List<IDefenceCard>(1);

                    cardsToSelect.Add(card);
                }

                if (cardToResolve == null)
                {
                    if (cardsToSelect != null)
                    {
                        cardToResolve = (IDefenceCard)context.Game.Dialog.Select(context, this, cardsToSelect.ToArray(),
                            new CountValidator<ICard>(0, 1), "Select a reaction card to use. None to skip all").SingleOrDefault();
                    }
                }

                if (cardToResolve == null)
                    break;

                continueAttack = continueAttack && cardToResolve.ResolveAttack(context, this);
                resolvedCards.Add(cardToResolve);
            }

            return continueAttack;
        }

        public void AfterBuy(TurnContext context, ICard boughtCard)
        {
            foreach (var card in this.PlayArea.OfType<IPostBuyOtherCardEventCard>())
            {
                card.AfterBuy(context, this, boughtCard);
            }
        }

        public void Cleanup(TurnContext context)
        {
            List<IPreCleanupSelfMovementCard> cardsToMove = null;
            foreach (var card in context.ActivePlayer.PlayArea.OfType<IPreCleanupSelfMovementCard>())
            {
                if (card.ShouldMove(context, context.ActivePlayer))
                {
                    if (cardsToMove == null)
                        cardsToMove = new List<IPreCleanupSelfMovementCard>(1);

                    cardsToMove.Add(card);
                }
            }
            if (cardsToMove != null)
            {
                foreach (var card in cardsToMove)
                {
                    card.MoveBeforeCleanup(context, context.ActivePlayer);
                }
            }
            
            this.PlayArea.Where(context.ShouldNotRemainInPlay).MoveAll(this.PlayArea, this.DiscardArea, CardMovementVerb.Discard, context);
            this.Hand.MoveAll(this.Hand, this.DiscardArea, CardMovementVerb.Discard, context);
            
            if (context.IsNextTurnExtraTurnForActivePlayer && this == context.ActivePlayer)
            {
                this.DrawCards(3, context);
            }
            else
            {
                this.DrawCards(5, context);
            }
        }

        public void AddDurationEffect(IDurationEffect effect)
        {
            this.durationEffects.Add(effect);
        }

        public void AddGainedCardName(string cardName, TurnContext context)
        {
            if (context.ActivePlayer == this)
            {
                this.recentGainedCardNamesOnMyTurn.Add(cardName);
            }
        }

        public bool HasRecentlyGainedOnSelfTurn(CardSupplyPile pile)
        {
            return this.recentGainedCardNamesOnMyTurn.Contains(pile.Name);
        }

        public int Score()
        {
            var allCards = this.deck
                .Concat(this.Hand)
                .Concat(this.PlayArea)
                .Concat(this.DiscardArea)
                .Concat(this.SetAsideArea)
                .Concat(this.NativeVillageMat);

            return allCards.OfType<IScoringCard>().Sum(x => x.Score(allCards)) + this.VPTokens;
        }

        private void Shuffle<T>(T[] array)
        {
            for (int i = 0; i < 3; i++)
            {
                CollectionUtil.Shuffle(array, random);
            }
        }
        #endregion
    }
}