using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using IdeaFactory.Util;
using Newtonsoft.Json;
using Powercards.Core;
using Powercards.Core.Cards;

namespace Powercards.WinPresent
{
    internal class GameDialog : IGameDialog
    {
        #region fields
        private readonly IChannel channel;
        private readonly Dictionary<string, bool> bulkPlayableNamesCache;
        private int currentGameProgressID;
        #endregion

        #region constructors
        public GameDialog(IChannel channel)
        {
            Enforce.ArgumentNotNull(channel);
            this.channel = channel;
            
            this.bulkPlayableNamesCache = new Dictionary<string, bool>(StringComparer.Ordinal);
        }
        #endregion

        #region dialog methods
        public T[] Select<T>(TurnContext context, Player dialogPlayer, IEnumerable<T> items, IValidator<IEnumerable<T>> validator, string description)
            where T : INameIdentifiable
        {
            currentGameProgressID++;
            IGameProgressor progressor = null;

            do
            {
                var dialogView = new DialogViewModel { PlayerName = dialogPlayer.Name, Description = description, Selection = items.Select(x => x.Name).ToArray(), };

                if (progressor != null && CanProgress(progressor, dialogPlayer, dialogView))
                {
                    T[] selected;
                    if ("b".Equals(progressor.Message, StringComparison.OrdinalIgnoreCase))
                    {
                        selected = items.Where(this.IsBulkPlayable).ToArray();
                        if (selected.Length == 0)
                        {
                            dialogView.ErrorMessage = "There are no items to be bulk playable";
                            selected = null;
                        }
                    }
                    else
                    {
                        var names = SplitCsv(progressor.Message);
                        selected = FindItems(names, items);
                        if (selected.Length != names.Length)
                        {
                            dialogView.ErrorMessage = "Cannot find all the items you specified";
                            selected = null;
                        }
                    }

                    if (selected != null)
                    {
                        if (!validator.Validate(selected))
                        {
                            dialogView.ErrorMessage = "Validation failed";
                            selected = null;
                        }
                    }

                    if (selected != null)
                        return selected;
                }

                channel.SnapshotReady(CreateGameSnapshot(context, dialogView));
                progressor = channel.WaitForProgressor();
            }
            while (true);
        }

        public bool Should(TurnContext context, Player dialogPlayer, string description)
        {
            currentGameProgressID++;
            IGameProgressor progressor = null;

            do
            {
                var dialogView = new DialogViewModel { PlayerName = dialogPlayer.Name, Description = description, };

                if (progressor != null && CanProgress(progressor, dialogPlayer, dialogView))
                {
                    if ("yes".Equals(progressor.Message, StringComparison.OrdinalIgnoreCase))
                        return true;

                    if ("no".Equals(progressor.Message, StringComparison.OrdinalIgnoreCase))
                        return false;

                    bool result;
                    if (bool.TryParse(progressor.Message, out result))
                        return result;

                    dialogView.ErrorMessage = "Unknown answer";
                }

                channel.SnapshotReady(CreateGameSnapshot(context, dialogView));
                progressor = channel.WaitForProgressor();
            }
            while (true);
        }

        public int[] Choose(TurnContext context, Player dialogPlayer, string[] choices, int numberOfChoicesToChoose, string description)
        {
            currentGameProgressID++;
            IGameProgressor progressor = null;

            do
            {
                var dialogView = new DialogViewModel { PlayerName = dialogPlayer.Name, Description = description, Selection = CreateOrderedSelection(choices), };

                if (progressor != null && CanProgress(progressor, dialogPlayer, dialogView))
                {
                    var texts = SplitCsv(progressor.Message);
                    var indexes = ParseIndexes(texts, 0, choices.Length - 1);
                    if (indexes.Length == numberOfChoicesToChoose)
                        return indexes;

                    dialogView.ErrorMessage = "You have to choose exactly " + numberOfChoicesToChoose + " choices";
                }

                channel.SnapshotReady(CreateGameSnapshot(context, dialogView));
                progressor = channel.WaitForProgressor();
            }
            while (true);
        }

        public string Name(TurnContext context, Player dialogPlayer, string description)
        {
            currentGameProgressID++;
            IGameProgressor progressor = null;

            do
            {
                var dialogView = new DialogViewModel { PlayerName = dialogPlayer.Name, Description = description, };

                if (progressor != null && CanProgress(progressor, dialogPlayer, dialogView))
                {
                    if (!string.IsNullOrEmpty(progressor.Message))
                        return progressor.Message;

                    dialogView.ErrorMessage = "You must type something";
                }

                channel.SnapshotReady(CreateGameSnapshot(context, dialogView));
                progressor = channel.WaitForProgressor();
            }
            while (true);
        }

        private string CreateGameSnapshot(TurnContext context, DialogViewModel dialogView)
        {
            var builder = new StringBuilder(1024);
            using (var writer = new StringWriter(builder))
            {
                using (var j = new JsonTextWriter(writer))
                {
                    #if DEBUG
                    j.Formatting = Formatting.Indented;
                    #endif

                    j.WriteStartObject();

                    j.WritePropertyName("GameProgressID");
                    j.WriteValue(currentGameProgressID);

                    j.WritePropertyName("Dialog");
                    j.WriteStartObject();
                    j.WritePropertyName("PlayerName"); 
                    j.WriteValue(dialogView.PlayerName);

                    j.WritePropertyName("Description"); 
                    j.WriteValue(dialogView.Description);
                    
                    if (dialogView.Selection != null)
                    {
                        j.WritePropertyName("Selection");
                        j.WriteStartArray();
                        foreach (var item in dialogView.Selection)
                        {
                            j.WriteValue(item);
                        }
                        j.WriteEnd();
                    }
                    
                    if (!string.IsNullOrEmpty(dialogView.ErrorMessage))
                    {
                        j.WritePropertyName("ErrorMessage");
                        j.WriteValue(dialogView.ErrorMessage);
                    }

                    j.WriteEndObject();

                    j.WriteEndObject();
                }
            }

            //foreach (var player in context.Players)
            //{
            //    snapshot.Append(player.Name);
            //    snapshot.AppendLine();
                
            //    // in play
            //    snapshot.Append("In Play:");
            //    foreach (var card in player.PlayArea)
            //    {
            //        snapshot.Append(card.Name);
            //        snapshot.Append(Chars.Comma);
            //    }
            //    snapshot.AppendLine();

            //    // hand
            //    snapshot.Append("Hand:");
            //    foreach (var card in player.Hand)
            //    {
            //        snapshot.Append(card.Name);
            //        snapshot.Append(Chars.Comma);
            //    }
            //    snapshot.AppendLine();

            //    snapshot.Append("Hand Size:");
            //    snapshot.Append(player.Hand.CardCount);
            //    snapshot.AppendLine();

            //    // turn
            //    snapshot.Append("Turn:");
            //    snapshot.AppendLine(player.TurnCount.ToString());

            //    // active player public info
            //    if (player == context.ActivePlayer)
            //    {
            //        snapshot.Append("Actions:");
            //        snapshot.AppendLine(context.RemainingActions.ToString());

            //        snapshot.Append("Buys:");
            //        snapshot.AppendLine(context.Buys.ToString());

            //        snapshot.Append("Spend:$");
            //        snapshot.AppendLine(context.AvailableSpend.ToString());

            //        snapshot.Append("Played Actions:");
            //        snapshot.AppendLine(context.PlayedActions.ToString());

            //        snapshot.Append("Unused Actions:");
            //        snapshot.AppendLine(context.UnusedActions.ToString());
            //    }

            //    // other public info
            //    snapshot.Append("VP Tokens:");
            //    snapshot.AppendLine(player.VPTokens.ToString());

            //    snapshot.Append("Coin Tokens:");
            //    snapshot.AppendLine(player.CoinTokens.ToString());

            //    snapshot.Append("Deck Size:");
            //    snapshot.AppendLine(player.DeckCardCount.ToString());

            //    snapshot.Append("Discard Pile Size:");
            //    snapshot.AppendLine(player.DiscardArea.CardCount.ToString());

            //    // private info
            //    snapshot.Append("Set Aside:");
            //    foreach (var card in player.SetAsideArea)
            //    {
            //        snapshot.Append(card.Name);
            //        snapshot.Append(Chars.Comma);
            //    }
            //    snapshot.AppendLine();

            //    snapshot.Append("Native Village Mat:");
            //    foreach (var card in player.NativeVillageMat)
            //    {
            //        snapshot.Append(card.Name);
            //        snapshot.Append(Chars.Comma);
            //    }
            //    snapshot.AppendLine();
            //}

            //foreach (var pile in context.Game.Supply.AllPiles)
            //{
            //    if (!pile.IsEmpty)
            //    {
            //        snapshot.Append('$');
            //        snapshot.Append(pile.TopCard.GetCost(context, context.ActivePlayer));
            //    }
            //    else
            //    {
            //        snapshot.Append(Chars.Hyphen);
            //        snapshot.Append(Chars.Hyphen);
            //    }
            //    snapshot.Append(Chars.Space);

            //    snapshot.Append(pile.Name);
            //    snapshot.Append(Chars.Space);
            //    snapshot.Append(pile.CardCount);

            //    if (pile == context.Game.Supply.BanePile)
            //    {
            //        snapshot.Append(Chars.Space);
            //        snapshot.Append('B');
            //    }

            //    if (pile.EmbargoTokens > 0)
            //    {
            //        snapshot.Append(Chars.Space);
            //        snapshot.Append('E');
            //        snapshot.Append(Chars.Colon);
            //        snapshot.Append(pile.EmbargoTokens);
            //    }
            //    snapshot.AppendLine();
            //}

            return builder.ToString();
        }

        private bool CanProgress(IGameProgressor progressor, Player dialogPlayer, DialogViewModel dialogView)
        {
            if (dialogPlayer.Name.Equals(progressor.SenderName, StringComparison.OrdinalIgnoreCase))
            {
                if (currentGameProgressID == progressor.GameProgressID)
                    return true;

                dialogView.ErrorMessage = "Incorrect game progress ID";
            }
            return false;
        }
        #endregion

        #region bulk playing methods
        private bool IsBulkPlayable<T>(T item)
            where T : INameIdentifiable
        {
            bool result;
            if (bulkPlayableNamesCache.TryGetValue(item.Name, out result))
                return result;

            result = IsBulkPlayableImpl(item);
            bulkPlayableNamesCache.Add(item.Name, result);
            return result;
        }

        private static bool IsBulkPlayableImpl(INameIdentifiable item)
        {
            if (item is Copper || item is Silver || item is Gold || item is Platinum)
                return true;

            if (item is Harem || item is Hoard || item is Quarry || item is RoyalSeal || item is Talisman)
                return true;

            return false;
        }
        #endregion

        #region helper methods
        private static T[] FindItems<T>(IEnumerable<string> names, IEnumerable<T> items)
            where T : INameIdentifiable
        {
            var found = new HashSet<T>();
            foreach (var name in names)
            {
                foreach (var item in items)
                {
                    if (item.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
                    {
                        if (found.Add(item))
                            break;
                    }
                }
            }
            return found.ToArray();
        }

        private static int[] ParseIndexes(IEnumerable<string> texts, int minValue, int maxValue)
        {
            var indexes = new HashSet<int>();
            foreach (var text in texts)
            {
                int answer;
                if (int.TryParse(text, out answer))
                {
                    var index = answer - 1;
                    if (index >= minValue && index <= maxValue)
                    {
                        indexes.Add(index);
                    }
                }
            }
            return indexes.ToArray();
        }

        private static string[] SplitCsv(string text)
        {
            if (string.IsNullOrEmpty(text))
                return new string[0];

            var parts = text.Split(new[] { Chars.Comma }, StringSplitOptions.RemoveEmptyEntries);
            var list = new List<string>(parts.Length);
            foreach (var part in parts)
            {
                var item = part.Trim();
                if (!string.IsNullOrEmpty(item))
                {
                    list.Add(item);
                }
            }
            return list.ToArray();
        }

        private static string[] CreateOrderedSelection(string[] items)
        {
            var selection = new string[items.Length];
            for (int i = 0; i < selection.Length; i++)
            {
                selection[i] = string.Format("{0}: {1}", i + 1, items[i]);
            }
            return selection;
        }
        #endregion

        #region inner classes
        private class DialogViewModel
        {
            public string PlayerName { get; set; }
            public string Description { get; set; }
            public string[] Selection { get; set; }
            public string ErrorMessage { get; set; }
        }
        #endregion
    }
}
