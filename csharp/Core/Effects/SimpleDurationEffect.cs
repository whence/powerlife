using System;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class SimpleDurationEffect : IDurationEffect
    {
        #region fields
        private readonly Action<TurnContext> onTurnStarting;
        #endregion

        #region constructors
        public SimpleDurationEffect(Action<TurnContext> onTurnStarting)
        {
            Enforce.ArgumentNotNull(onTurnStarting);
            this.onTurnStarting = onTurnStarting;
        }
        #endregion

        #region methods
        public void OnTurnStarting(TurnContext context)
        {
            onTurnStarting(context);
        }
        #endregion
    }
}
