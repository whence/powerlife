using System.Collections;
using System.Collections.Generic;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class PlayerOwnedZone : CardZoneBase, IEnumerable<ICard>
    {
        #region fields
        private readonly Player owner;
        #endregion

        #region properties
        public Player Owner
        {
            get { return owner; }
        }
        #endregion

        #region constructors
        public PlayerOwnedZone(Player owner)
        {
            Enforce.ArgumentNotNull(owner);
            this.owner = owner;
        }
        #endregion

        #region methods
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
