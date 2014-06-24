using System;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public abstract class CardBase : ICard
    {
        #region properties
        public string Name { get; private set; }
        protected int OriginalCost { get; private set; }
        public ICardZone CurrentZone { get; private set; }
        #endregion

        #region constructors
        protected CardBase()
        {
            this.CurrentZone = SingletonWrapper<UnassignedCardZone>.Instance;
        }
        #endregion

        #region methods
        internal void Init(string name, int originalCost, ICardZone targetZone)
        {
            this.Name = name;
            this.OriginalCost = originalCost;
            MoveInternal(targetZone, CardMovementVerb.Init);
        }

        public virtual int GetCost(TurnContext context, Player player)
        {
            return context.CalculateCardCost(this, this.OriginalCost, player);
        }

        public bool MoveTo(ICardZone sourceZone, ICardZone oldTargetZone, CardMovementVerb verb, TurnContext context)
        {
            var newTargetZone = context.OnCardMovement(this, verb, oldTargetZone);
            Enforce.IsTrue(this.CurrentZone == sourceZone);
            Enforce.IsTrue(sourceZone != newTargetZone);
            MoveInternal(newTargetZone, verb);
            return newTargetZone == oldTargetZone;
        }

        private void MoveInternal(ICardZone targetZone, CardMovementVerb verb)
        {
            this.CurrentZone.RemoveCard(this, verb);
            this.CurrentZone = targetZone;
            targetZone.AddCard(this, verb);
        }
        #endregion

        #region inner classes
        private class UnassignedCardZone : ICardZone
        {
            public void AddCard(ICard card, CardMovementVerb verb)
            {
                throw new NotImplementedException();
            }

            public void RemoveCard(ICard card, CardMovementVerb verb)
            {
                Enforce.IsTrue(card.CurrentZone == this);
            }
        }
        #endregion
    }
}