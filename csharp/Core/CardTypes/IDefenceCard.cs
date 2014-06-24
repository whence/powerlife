namespace Powercards.Core
{
    /// <summary>
    /// This is not the same as reaction card, it's a subset of reaction card that only react to attack. 
    /// </summary>
    public interface IDefenceCard : ICard
    {
        #region properties
        bool IsDefenceOptional { get; }
        #endregion

        #region methods
        bool IsDefenceUsable(Player player);

        /// <returns>Can continue the attack?</returns>
        bool ResolveAttack(TurnContext context, Player player);
        #endregion
    }
}
