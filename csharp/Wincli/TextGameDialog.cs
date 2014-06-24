using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using IdeaFactory.Util;
using Powercards.Core;
using Powercards.Core.Cards;

namespace Powercards.Wincli
{
    public class TextGameDialog : IGameDialog
    {
        #region fields
        private readonly TextReader input;
        private readonly TextWriter output;
        private readonly Dictionary<string, bool> bulkPlayableNamesCache;
        #endregion

        #region constructors
        public TextGameDialog(TextReader input, TextWriter output)
        {
            Enforce.ArgumentNotNull(input);
            this.input = input;

            Enforce.ArgumentNotNull(output);
            this.output = output;

            this.bulkPlayableNamesCache = new Dictionary<string, bool>(StringComparer.Ordinal);
        }
        #endregion

        #region dialog methods
        public T[] Select<T>(TurnContext context, Player player, IEnumerable<T> items, IValidator<IEnumerable<T>> validator, string description)
            where T : INameIdentifiable
        {
            output.Write(player.Name);
            output.Write(Chars.Colon);
            output.WriteLine(description);
            foreach (var item in items)
            {
                output.WriteLine(item.Name);
            }

            while (true)
            {
                output.Write(player.Name);
                output.Write(Chars.Colon);
                var line = (input.ReadLine() ?? string.Empty).Trim();
                if (Intercept(player, context, line))
                    continue;

                T[] selected;
                if (line.Equals("b", StringComparison.OrdinalIgnoreCase))
                {
                    selected = items.Where(this.IsBulkPlayable).ToArray();
                    if (selected.Length == 0)
                    {
                        output.WriteLine("There are no items to be bulk playable");
                        continue;
                    }
                }
                else
                {
                    var names = SplitCsv(line);
                    selected = FindItems(names, items);
                    if (selected.Length != names.Length)
                    {
                        output.WriteLine("Cannot find all the items you specified");
                        continue;
                    }
                }

                if (!validator.Validate(selected))
                {
                    output.WriteLine("Validation failed");
                    continue;
                }

                return selected;
            }
        }

        public bool Should(TurnContext context, Player player, string description)
        {
            output.Write(player.Name);
            output.Write(Chars.Colon);
            output.WriteLine(description);

            while (true)
            {
                output.Write(player.Name);
                output.Write(Chars.Colon);
                var line = (input.ReadLine() ?? string.Empty).Trim();
                if (Intercept(player, context, line))
                    continue;

                if ("yes".Equals(line, StringComparison.OrdinalIgnoreCase))
                    return true;

                if ("no".Equals(line, StringComparison.OrdinalIgnoreCase))
                    return false;

                bool result;
                if (!bool.TryParse(line, out result))
                {
                    output.WriteLine("unknown answer");
                    continue;
                }

                return result;
            }
        }

        public int[] Choose(TurnContext context, Player player, string[] choices, int numberOfChoicesToChoose, string description)
        {
            output.Write(player.Name);
            output.Write(Chars.Colon);
            output.WriteLine(description);
            for (int i = 0; i < choices.Length; i++)
            {
                output.WriteLine("{0}: {1}", i + 1, choices[i]);
            }

            while (true)
            {
                output.Write(player.Name);
                output.Write(Chars.Colon);
                var line = (input.ReadLine() ?? string.Empty).Trim();
                if (Intercept(player, context, line))
                    continue;

                var texts = SplitCsv(line);
                var indexes = ParseIndexes(texts, 0, choices.Length - 1);
                if (indexes.Length != numberOfChoicesToChoose)
                {
                    output.WriteLine("You have to choose exactly " + numberOfChoicesToChoose + " choices");
                    continue;
                }

                return indexes;
            }
        }

        public string Name(TurnContext context, Player player, string description)
        {
            output.Write(player.Name);
            output.Write(Chars.Colon);
            output.WriteLine(description);

            while (true)
            {
                output.Write(player.Name);
                output.Write(Chars.Colon);
                var line = (input.ReadLine() ?? string.Empty).Trim();
                if (Intercept(player, context, line))
                    continue;

                if (string.IsNullOrEmpty(line))
                {
                    output.WriteLine("You must type something");
                    continue;
                }

                return line;
            }
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

        #region intercept methods
        private bool Intercept(Player player, TurnContext context, string line)
        {
            switch (line.ToLowerInvariant())
            {
                case "hand":
                    foreach (var card in player.Hand)
                    {
                        output.WriteLine(card.Name);
                    }
                    break;

                case "supply":
                    foreach (var pile in context.Game.Supply.AllPiles)
                    {
                        if (!pile.IsEmpty)
                        {
                            output.Write('$');
                            output.Write(pile.TopCard.GetCost(context, context.ActivePlayer));
                        }
                        else
                        {
                            output.Write(Chars.Hyphen);
                            output.Write(Chars.Hyphen);
                        }
                        output.Write(Chars.Space);

                        output.Write(pile.Name);
                        output.Write(Chars.Space);
                        output.Write(pile.CardCount);

                        if (pile == context.Game.Supply.BanePile)
                        {
                            output.Write(Chars.Space);
                            output.Write('B');
                        }

                        if (pile.EmbargoTokens > 0)
                        {
                            output.Write(Chars.Space);
                            output.Write('E');
                            output.Write(Chars.Colon);
                            output.Write(pile.EmbargoTokens);
                        }
                        output.WriteLine();
                    }
                    break;

                case "info":
                    output.Write("Turn:");
                    output.WriteLine(player.TurnCount);

                    if (player == context.ActivePlayer)
                    {
                        output.Write("Actions:");
                        output.WriteLine(context.RemainingActions);

                        output.Write("Buys:");
                        output.WriteLine(context.Buys);

                        output.Write("Spend:$");
                        output.WriteLine(context.AvailableSpend);
                        
                        output.Write("Played Actions:");
                        output.WriteLine(context.PlayedActions);

                        output.Write("Unused Actions:");
                        output.WriteLine(context.UnusedActions);
                    }

                    output.Write("VP Tokens:");
                    output.WriteLine(player.VPTokens);

                    output.Write("Deck Size:");
                    output.WriteLine(player.DeckCardCount);

                    output.Write("Hand Size:");
                    output.WriteLine(player.Hand.CardCount);

                    output.Write("Discard Pile Size:");
                    output.WriteLine(player.DiscardArea.CardCount);

                    output.Write("Coin Tokens:");
                    output.WriteLine(player.CoinTokens);

                    output.Write("In Play:");
                    foreach (var card in player.PlayArea)
                    {
                        output.Write(card.Name);
                        output.Write(Chars.Comma);
                        output.Write(Chars.Space);
                    }
                    output.WriteLine();

                    output.Write("Set Aside:");
                    foreach (var card in player.SetAsideArea)
                    {
                        output.Write(card.Name);
                        output.Write(Chars.Comma);
                        output.Write(Chars.Space);
                    }
                    output.WriteLine();

                    output.Write("Native Village Mat:");
                    foreach (var card in player.NativeVillageMat)
                    {
                        output.Write(card.Name);
                        output.Write(Chars.Comma);
                        output.Write(Chars.Space);
                    }
                    output.WriteLine();

                    break;

                case "exit":
                    Environment.Exit(0);
                    // ReSharper disable HeuristicUnreachableCode
                    break;
                    // ReSharper restore HeuristicUnreachableCode

                default:
                    return false;
            }

            return true;
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
        #endregion
    }
}
