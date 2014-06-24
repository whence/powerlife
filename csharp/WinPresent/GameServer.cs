using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reactive.Subjects;
using System.Text;
using System.Threading.Tasks;
using IdeaFactory.Util;
using Powercards.Core;

namespace Powercards.WinPresent
{
    public class GameServer : IDisposable, IChannel
    {
        #region fields
        private readonly BlockingCollection<GameProgressor> progressors;
        private Task task;

        private StringBuilder logBuffer;
        private readonly Subject<string> logUpdates;
        private readonly Subject<string> snapshotUpdates;
        #endregion

        #region properties
        public IObservable<string> LogUpdates
        {
            get { return logUpdates; }
        }

        public IObservable<string> SnapshotUpdates
        {
            get { return snapshotUpdates; }
        }
        #endregion

        #region constructors
        public GameServer()
        {
            this.progressors = new BlockingCollection<GameProgressor>();
            this.logUpdates = new Subject<string>();
            this.snapshotUpdates = new Subject<string>();
        }
        #endregion

        #region methods
        public void Start(IEnumerable<string> playerNames)
        {
            Enforce.ArgumentNotNull(playerNames);
            
            if (task == null)
            {
                task = Task.Factory.StartNew(this.RunGame, 
                    new List<string>(playerNames).ToArray(), // make a copy of it 
                    TaskCreationOptions.LongRunning);
            }
        }

        private void Stop()
        {
            if (task != null)
            {
                this.progressors.CompleteAdding();

                try
                {
                    task.Wait();
                }
                catch (AggregateException e)
                {
                    if (e.InnerException != null && e.InnerException is GameServerAbortionException)
                    {
                    }
                    else
                    {
                        throw;
                    }
                }
                
                task.Dispose();
                task = null;
            }
        }

        public void Dispose()
        {
            this.Stop();
            this.progressors.Dispose();
            this.logUpdates.Dispose();
            this.snapshotUpdates.Dispose();
        }

        public void Progress(string senderName, int gameProgressID, string message)
        {
            this.progressors.Add(new GameProgressor(senderName, gameProgressID, message));
        }

        private void RunGame(object state)
        {
            var playerNames = (IEnumerable<string>)state;
            var supplyCreator = new RandomCardSupplyCreator { NumberOfPlayers = playerNames.Count(), };
            this.logBuffer = new StringBuilder(512);
            var log = new TextGameLog(new StringWriter(logBuffer));
            var dialog = new GameDialog(this);
            new Game(playerNames, supplyCreator, log, dialog).Run();
        }

        IGameProgressor IChannel.WaitForProgressor()
        {
            var logUpdate = this.logBuffer.ToString();
            this.logBuffer.Clear();

            if (!string.IsNullOrEmpty(logUpdate))
                logUpdates.OnNext(logUpdate);

            try
            {
                if (!progressors.IsCompleted)
                    return progressors.Take();
            }
            catch (InvalidOperationException)
            {
            }

            logUpdates.OnCompleted();
            return Abort();
        }

        void IChannel.SnapshotReady(string snapshot)
        {
            snapshotUpdates.OnNext(snapshot);
        }

        private static GameProgressor Abort()
        {
            throw new GameServerAbortionException();
        }
        #endregion

        #region inner classes
        [Serializable]
        private class GameServerAbortionException : Exception
        {
        }

        private class GameProgressor : IGameProgressor
        {
            #region fields
            private readonly string senderName;
            private readonly int gameProgressID;
            private readonly string message;
            #endregion

            #region properties
            public string SenderName { get { return senderName; } }
            public int GameProgressID { get { return gameProgressID; } }
            public string Message { get { return message; } }
            #endregion

            #region constructors
            public GameProgressor(string senderName, int gameProgressID, string message)
            {
                Enforce.ArgumentNotEmptyOrNull(senderName);
                this.senderName = senderName;

                Enforce.ArgumentValid(gameProgressID > 0);
                this.gameProgressID = gameProgressID;

                this.message = message;
            }
            #endregion
        }
        #endregion
    }
}
