using System.IO;
using IdeaFactory.Util;
using Powercards.Core;

namespace Powercards.Wincli
{
    internal class Application
    {
        #region fields
        private readonly TextReader input;
        private readonly TextWriter output;
        #endregion

        #region constructors
        public Application(TextReader input, TextWriter output)
        {
            Enforce.ArgumentNotNull(input);
            this.input = input;

            Enforce.ArgumentNotNull(output);
            this.output = output;
        }
        #endregion

        #region methods
        public void Run()
        {
            var playerNames = new[] { "Wes", "Becky", };
            CardSupplyCreator supplyCreator;
            //supplyCreator = new FixedCardSupplyCreator { NumberOfPlayers = playerNames.Length, UseColonyPlatinum = false, };
            supplyCreator = new RandomCardSupplyCreator { NumberOfPlayers = playerNames.Length, };
            var log = new TextGameLog(output); // to get full log, use a stringbuilder backed textwriter, then builder.ToString upon web response
            var dialog = new TextGameDialog(input, output);

            new Game(playerNames, supplyCreator, log, dialog).Run();

            output.Write("Press Enter to end the application");
            input.ReadLine();
        }
        #endregion
    }
}
