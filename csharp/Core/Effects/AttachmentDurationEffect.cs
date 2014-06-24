namespace Powercards.Core
{
    public class AttachmentDurationEffect : IDurationEffect
    {
        #region properties
        public ICard AttachedCard { private get; set; }
        public bool IsAttachedCardHidden { private get; set; }
        #endregion

        #region methods
        public void OnTurnStarting(TurnContext context)
        {
            if (this.AttachedCard != null)
            {
                if (this.AttachedCard.MoveTo(context.ActivePlayer.SetAsideArea, context.ActivePlayer.Hand, CardMovementVerb.PutInHand, context))
                {
                    if (this.IsAttachedCardHidden)
                    {
                        context.Game.Log.LogMessage(context.ActivePlayer.Name + " put a set-asided card in hand");
                    }
                    else
                    {
                        context.Game.Log.LogMessage("{0} put {1} in hand", context.ActivePlayer.Name, this.AttachedCard.Name);
                    }
                }
            }
        }
        #endregion
    }
}
