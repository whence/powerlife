using System.Collections;
using System.Collections.Generic;
using Powercards.Core.Cards;

namespace Powercards.Core
{
    public class PrizeZone : CardZoneBase, IEnumerable<ICard>
    {
        #region methods
        public void Init()
        {
            CardCreator.Create(typeof(BagOfGold), this);
            CardCreator.Create(typeof(Diadem), this);
            CardCreator.Create(typeof(Followers), this);
            CardCreator.Create(typeof(Princess), this);
            CardCreator.Create(typeof(TrustySteed), this);
        }

        public IEnumerator<ICard> GetEnumerator()
        {
            return this.GetCardEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return this.GetCardEnumerator();
        }
        #endregion
    }
}
