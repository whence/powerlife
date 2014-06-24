namespace Powercards.WinPresent
{
    public interface IGameProgressor
    {
        #region properties
        string SenderName { get; }
        int GameProgressID { get; }
        string Message { get; }
        #endregion
    }
}
