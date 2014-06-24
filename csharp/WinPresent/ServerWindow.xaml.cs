using System;
using System.ComponentModel;
using System.Net;
using System.Reactive.Linq;
using System.Windows;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Powercards.WinPresent
{
    public partial class ServerWindow
    {
        #region fields
        private readonly GameServer server;
        private readonly Communicator communicator;
        private readonly IDisposable[] subscriptions;
        private string latestSnapshot;
        #endregion

        #region constructors
        public ServerWindow()
        {
            InitializeComponent();

            this.server = new GameServer();
            this.communicator = new Communicator(1024, this.Dispatcher, this.MessageReceived, Communicator.OperationMode.BrowseOnly);
            this.subscriptions =
                new[] {
                    this.server.LogUpdates.ObserveOnDispatcher().Subscribe(this.AppendLog),
                    this.server.SnapshotUpdates.ObserveOnDispatcher().Subscribe(this.SnapshotUpdated),
                    this.communicator.NewRemoteEndPointUpdates.Subscribe(this.NewRemoteEndPointUpdated),
            };
        }
        #endregion

        #region methods
        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            this.communicator.Start();
        }

        private void btnStartGame_Click(object sender, RoutedEventArgs e)
        {
            this.server.Start(new[] { "Wes", "Becky", });
            btnStartGame.Content = "Game Running";
            btnStartGame.IsEnabled = false;
        }

        private void MessageReceived(string text)
        {
            var snapshot = (JObject)JsonConvert.DeserializeObject(text);

            this.server.Progress(
                snapshot["PlayerName"].Value<string>(),
                snapshot["GameProgressID"].Value<int>(),
                snapshot["Message"].Value<string>());
        }

        private void AppendLog(string text)
        {
            txtLog.Text += text + Environment.NewLine;
            txtLog.ScrollToEnd();
        }

        private void SnapshotUpdated(string snapshot)
        {
            this.latestSnapshot = snapshot;
            BroadcastSnapshot();
        }

        private void BroadcastSnapshot()
        {
            foreach (var endPoint in this.communicator.GetResolvedRemoteServiceEndPoints())
            {
                SendSnapshot(endPoint);
            }
        }

        private void NewRemoteEndPointUpdated(IPEndPoint endPoint)
        {
            this.AppendLog("New Client Connected");
            SendSnapshot(endPoint);
        }

        private void SendSnapshot(IPEndPoint endPoint)
        {
            if (!string.IsNullOrEmpty(this.latestSnapshot))
            {
                this.communicator.Send(this.latestSnapshot, endPoint);
            }
        }

        protected override void OnClosing(CancelEventArgs e)
        {
            base.OnClosing(e);

            if (!e.Cancel)
            {
                this.server.Dispose();

                foreach (var subscription in this.subscriptions)
                {
                    subscription.Dispose();
                }

                this.communicator.Dispose();
            }
        }
        #endregion
    }
}
