using System;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class SharedTransitionalZone : CardZoneBase, IDisposable
    {
        #region methods
        public void Dispose()
        {
            Enforce.IsTrue(this.IsEmpty, "Transitional Zone is not empty. You will lose these cards if you don't move them out of the transitional zone");
        }
        #endregion
    }
}
