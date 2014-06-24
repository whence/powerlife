using System;
using System.Collections.Generic;
using System.IO;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class TextGameLog : IGameLog
    {
        #region fields
        private readonly TextWriter writer;
        #endregion

        #region constructors
        public TextGameLog(TextWriter writer)
        {
            this.writer = writer;
        }
        #endregion

        #region methods
        public void LogTurn(Player player)
        {
            writer.WriteLine("------------");
            writer.WriteLine("{0}'s turn has begun.", player.Name);
        }

        public void LogPlay(Player player, ICard card)
        {
            writer.WriteLine("{0} played a {1}.", player.Name, card.Name);
        }

        public void LogGameEnd(IEnumerable<Tuple<Player, int>> players, IEnumerable<Tuple<Player, int>> winners)
        {
            writer.WriteLine("The game has ended!");

            writer.WriteLine("SCORES");
            foreach (var player in players)
            {
                writer.WriteLine("{0}: {1}", player.Item1.Name, player.Item2);
            }
            writer.WriteLine();

            writer.WriteLine("WINNERS");
            foreach (var winner in winners)
            {
                writer.WriteLine(winner.Item1.Name);
            }
        }

        public void LogDiscard(Player player, ICard card)
        {
            writer.WriteLine("{0} discarded a {1}.", player.Name, card.Name);
        }

        public void LogMessage(string message)
        {
            writer.WriteLine(message);
        }
        
        public void LogMessage(string message, params object[] values)
        {
            writer.WriteLine(message, values);
        }

        public void LogBuy(Player player, CardSupplyPile pile)
        {
            writer.WriteLine("{0} bought a {1}.", player.Name, pile.Name);
        }

        public void LogGain(Player player, CardSupplyPile pile)
        {
            writer.WriteLine("{0} gained a {1}", player.Name, pile.Name);
        }

        public void LogGain(Player player, ICard card)
        {
            writer.WriteLine("{0} gained a {1}", player.Name, card.Name);
        }

        public void LogTrash(Player player, ICard card)
        {
            writer.WriteLine("{0} trashed a {1}.", player.Name, card.Name);
        }

        public void LogRevealHand(Player player)
        {
            writer.Write(player.Name);
            writer.Write(" has the following cards in hand: ");
            foreach (var card in player.Hand)
            {
                writer.Write(card.Name);
                writer.Write(Chars.Comma);
                writer.Write(Chars.Space);
            }
        }

        public void LogPutBack(Player player, ICard card)
        {
            writer.WriteLine("{0} put a {1} on top of the deck.", player.Name, card.Name);
        }
        #endregion
    }
}