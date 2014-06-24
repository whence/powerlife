using System;
using System.Linq;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class TransitionalZone : PlayerOwnedZone, IDisposable
    {
        #region constructors
        public TransitionalZone(Player owner)
            : base(owner)
        {
        }
        #endregion

        #region methods
        public void Reveal(IGameLog log)
        {
            if (!this.IsEmpty)
            {
                log.LogMessage("{0} revealed {1}.", this.Owner.Name, string.Join(", ", this.Select(c => c.Name).ToArray()));
            }
            else
            {
                log.LogMessage("{0} revealed nothing.", this.Owner.Name);
            }
        }

        public void Dispose()
        {
            Enforce.IsTrue(this.IsEmpty, "Transitional Zone is not empty. You will lose these cards if you don't move them out of the transitional zone");
        }
        #endregion
    }
}
