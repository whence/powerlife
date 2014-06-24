namespace Powercards.Core
{
    public class Deck : PlayerOwnedZone
    {
        #region properties
        public ICard TopCard
        {
            get { return IsEmpty ? null : this.GetCard(this.CardCount - 1); }
        }

        public ICard BottomCard
        {
            get { return IsEmpty ? null : this.GetCard(0); }
        }
        #endregion

        #region constructors
        public Deck(Player owner)
            : base(owner)
        {
        }
        #endregion

        #region methods
        protected override int? GetInsertIndex(CardMovementVerb verb)
        {
            switch (verb)
            {
                case CardMovementVerb.PutBackToDeckBottom:
                    return 0;

                default:
                    return base.GetInsertIndex(verb);
            }
        }
        #endregion
    }
}
