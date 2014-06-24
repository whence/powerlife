using System;

namespace Powercards.Core
{
    [AttributeUsage(AttributeTargets.Class, AllowMultiple = false, Inherited = false)]
    public class CardInfoAttribute : Attribute
    {
        #region fields
        private readonly CardSet cardSet;
        private readonly int originalCost;
        private readonly bool isKingdomCard;
        #endregion

        #region properties
        public CardSet CardSet
        {
            get { return cardSet; }
        }

        public int OriginalCost
        {
            get { return originalCost; }
        }

        public bool IsKingdomCard
        {
            get { return isKingdomCard; }
        }
        #endregion

        #region constructors
        public CardInfoAttribute(CardSet cardSet, int originalCost, bool isKingdomCard)
        {
            this.cardSet = cardSet;
            this.originalCost = originalCost;
            this.isKingdomCard = isKingdomCard;
        }
        #endregion
    }
}
