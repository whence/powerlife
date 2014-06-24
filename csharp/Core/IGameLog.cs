using System;
using System.Collections.Generic;

namespace Powercards.Core
{
    public interface IGameLog
    {
        #region methods
        void LogTurn(Player player);
        void LogPlay(Player player, ICard card);
        void LogDiscard(Player player, ICard card);
        void LogMessage(string message);
        void LogMessage(string message, params object[] values);
        void LogBuy(Player player, CardSupplyPile pile);
        void LogGain(Player player, CardSupplyPile pile);
        void LogGain(Player player, ICard card);
        void LogTrash(Player player, ICard card);
        void LogRevealHand(Player player);
        void LogPutBack(Player player, ICard card);
        void LogGameEnd(IEnumerable<Tuple<Player, int>> players, IEnumerable<Tuple<Player, int>> winners);
        #endregion
    }
}
