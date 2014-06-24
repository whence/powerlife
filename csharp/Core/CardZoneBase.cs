using System.Collections.Generic;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public abstract class CardZoneBase : ICardZone
    {
        #region fields
        private readonly List<ICard> cards;
        #endregion

        #region properties
        public bool IsEmpty
        {
            get { return (this.cards.Count == 0); }
        }

        public int CardCount
        {
            get { return this.cards.Count; }
        }
        #endregion

        #region constructors
        protected CardZoneBase()
        {
            this.cards = new List<ICard>();
        }
        #endregion

        #region methods
        public void AddCard(ICard card, CardMovementVerb verb)
        {
            Enforce.IsTrue(card.CurrentZone == this);

            var index = GetInsertIndex(verb);
            if (index.HasValue)
            {
                this.cards.Insert(index.Value, card);
            }
            else
            {
                this.cards.Add(card);
            }
        }

        protected virtual int? GetInsertIndex(CardMovementVerb verb)
        {
            return null;
        }

        public void RemoveCard(ICard card, CardMovementVerb verb)
        {
            Enforce.IsTrue(card.CurrentZone == this);
            Enforce.IsTrue(this.cards.Remove(card));
        }

        protected ICard GetCard(int index)
        {
            return this.cards[index];
        }

        protected IEnumerator<ICard> GetCardEnumerator()
        {
            return this.cards.GetEnumerator();
        }
        #endregion
    }
}
