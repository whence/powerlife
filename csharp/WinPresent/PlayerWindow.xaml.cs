using System;
using System.ComponentModel;
using System.IO;
using System.Net;
using System.Text;
using System.Windows;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Powercards.WinPresent
{
    public partial class PlayerWindow
    {
        #region fields
        private string playerName;
        private Communicator communicator;
        private int gameProgressID;
        private IPEndPoint serverEndPoint;
        #endregion

        #region constructors
        public PlayerWindow()
        {
            InitializeComponent();
        }
        #endregion

        #region lifecycle methods
        private void btnSubmit_Click(object sender, RoutedEventArgs e)
        {
            txtErrorMessage.Text = string.Empty;

            var message = (txtInput.Text ?? string.Empty).Trim();
            txtInput.Clear();

            if (this.communicator == null)
            {
                this.playerName = message;
                if (string.IsNullOrEmpty(this.playerName))
                {
                    txtErrorMessage.Text = "Empty player name";
                    btnSubmit.IsEnabled = true;
                }
                else
                {
                    this.Title = this.playerName;
                    this.communicator = new Communicator(1024, this.Dispatcher, this.MessageReceived, Communicator.OperationMode.PublishOnly);
                    this.communicator.Start();
                    btnSubmit.Content = "Submit";
                    btnSubmit.IsEnabled = false;
                }
            }
            else
            {
                var builder = new StringBuilder(32);
                using (var writer = new StringWriter(builder))
                {
                    using (var j = new JsonTextWriter(writer))
                    {
                        #if DEBUG
                        j.Formatting = Formatting.Indented;
                        #endif

                        j.WriteStartObject();

                        j.WritePropertyName("PlayerName");
                        j.WriteValue(this.playerName);

                        j.WritePropertyName("GameProgressID");
                        j.WriteValue(this.gameProgressID);

                        j.WritePropertyName("Message");
                        j.WriteValue(message);

                        j.WriteEndObject();
                    }
                }
                
                this.communicator.Send(builder.ToString(), this.serverEndPoint);
                btnSubmit.IsEnabled = false;
            }
        }

        private void MessageReceived(string text, IPEndPoint remoteEndPoint)
        {
            this.serverEndPoint = remoteEndPoint;

            var snapshot = (JObject)JsonConvert.DeserializeObject(text);
            if (snapshot["GameProgressID"].Value<int>() > this.gameProgressID)
            {
                this.gameProgressID = snapshot["GameProgressID"].Value<int>();

                if (snapshot["Dialog"]["PlayerName"].Value<string>().Equals(this.playerName, StringComparison.OrdinalIgnoreCase))
                {
                    var description = snapshot["Dialog"]["Description"].Value<string>();

                    txtDialogView.Text = description;
                    btnSubmit.IsEnabled = true;
                }
                else
                {
                    txtDialogView.Text = "Waiting for others";
                    btnSubmit.IsEnabled = false;
                }
            }
        }

        protected override void OnClosing(CancelEventArgs e)
        {
            base.OnClosing(e);

            if (!e.Cancel)
            {
                this.communicator.Dispose();
            }
        }
        #endregion
    }
}
