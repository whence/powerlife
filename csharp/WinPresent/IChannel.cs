namespace Powercards.WinPresent
{
    public interface IChannel
    {
        #region methods
        IGameProgressor WaitForProgressor();
        void SnapshotReady(string snapshot);
        #endregion
    }
}
