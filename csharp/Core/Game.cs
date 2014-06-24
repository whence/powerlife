using System;
using System.Collections.Generic;
using System.Linq;
using IdeaFactory.Util;
using Powercards.Core.Cards;

namespace Powercards.Core
{
    public class Game
    {
        #region fields
        private readonly Player[] allPlayers;
        private readonly IEnumerator<Player> playerLoop;
        private readonly CardSupply supply;
        private readonly TrashZone trashZone;
        private readonly PrizeZone prizeZone;
        private readonly IGameLog log;
        private readonly IGameDialog dialog;
        private GameStage stage;
        #endregion

        #region properties
        public CardSupply Supply
        {
            get { return supply; }
        }

        public TrashZone TrashZone
        {
            get { return trashZone; }
        }

        public PrizeZone PrizeZone
        {
            get { return prizeZone; }
        }

        public IGameLog Log
        {
            get { return log; }
        }

        public IGameDialog Dialog
        {
            get { return dialog; }
        }

        public int TradeRouteTokens { get; set; }
        #endregion

        #region constructors
        public Game(IEnumerable<string> playerNames, CardSupplyCreator supplyCreator, IGameLog log, IGameDialog dialog)
        {
            this.stage = GameStage.NotStarted;
            
            Enforce.ArgumentNotNull(playerNames);
            this.allPlayers = playerNames.Select(x => new Player(x)).ToArray();
            Enforce.ArgumentValid(this.allPlayers.Length >= 1 && this.allPlayers.Length <= 4, "Only 1 - 4 players are allowed");
            Enforce.ArgumentValid(playerNames.Distinct(StringComparer.OrdinalIgnoreCase).ToArray().Length == this.allPlayers.Length, "player name must be unique");
            this.playerLoop = new LoopEnumerator<Player>(this.allPlayers, new Random(Maths.RandomInt32()).Next(this.allPlayers.Length), null);

            Enforce.ArgumentNotNull(supplyCreator);
            this.supply = supplyCreator.CreateSupply();

            this.trashZone = new TrashZone();

            this.prizeZone = new PrizeZone();
            this.prizeZone.Init();

            Enforce.ArgumentNotNull(log);
            this.log = log;

            Enforce.ArgumentNotNull(dialog);
            this.dialog = dialog;

            foreach (var player in this.allPlayers)
            {
                player.Init();
            }
        }
        #endregion

        #region methods
        public void Run()
        {
            if (this.stage != GameStage.NotStarted)
                throw new CustomException<Game>("The game has already started. Create a new game instead");
            
            this.stage = GameStage.Running;

            while (this.stage == GameStage.Running && this.playerLoop.MoveNext())
            {
                TurnContext context = null;
                do
                {
                    context = new TurnContext(this, this.allPlayers, this.playerLoop.Current,
                        (context != null ? context.IsNextTurnExtraTurnForActivePlayer : false));

                    context.Run();

                    if (this.supply.ShouldEndGame())
                    {
                        Finish();
                        this.stage = GameStage.Finished;
                    }
                }
                while (this.stage == GameStage.Running && context.IsNextTurnExtraTurnForActivePlayer);
            }
        }

        private void Finish()
        {
            Enforce.IsTrue(this.stage == GameStage.Running);

            var scores = new List<Tuple<Player, int>>(this.allPlayers.Length);
            foreach (var player in this.allPlayers)
            {
                scores.Add(new Tuple<Player, int>(player, player.Score()));
            }
            Func<Tuple<Player, int>, int> firstCondition = x => x.Item2;
            Func<Tuple<Player, int>, int> secondCondition = x => x.Item1.TurnCount;
            scores = scores.OrderByDescending(firstCondition).ThenBy(secondCondition).ToList();
            var winner = scores.First();
            var winners = new List<Tuple<Player, int>>();
            foreach (var score in scores)
            {
                if (firstCondition(score) == firstCondition(winner)
                    && secondCondition(score) == secondCondition(winner))
                {
                    winners.Add(score);
                }
            }
            this.log.LogGameEnd(scores, winners);
        }

        public Player NextPlayerOf(Player player)
        {
            Enforce.IsTrue(this.stage == GameStage.Running);
            
            return new LoopEnumerator<Player>(
                this.allPlayers, Array.IndexOf(this.allPlayers, player),
                2).AsEnumerable().Skip(1).First();
        }

        public void AfterBuy(TurnContext context, Player player, CardSupplyPile pile)
        {
            Enforce.IsTrue(this.stage == GameStage.Running);

            if (pile.EmbargoTokens > 0)
            {
                var cursePile = context.Game.Supply.AllPiles.FirstOrDefault(new NonEmptyPileValidator().Then(new CardTypeValidator<Curse>()).Validate);
                for (int i = 0; i < pile.EmbargoTokens; i++)
                {
                    if (cursePile != null && !cursePile.IsEmpty)
                    {
                        if (cursePile.TopCard.MoveTo(cursePile, player.DiscardArea, CardMovementVerb.Gain, context))
                            context.Game.Log.LogGain(player, cursePile);
                    }
                }
            }
        }
        #endregion

        #region inner classes
        private enum GameStage
        {
            NotStarted,
            Running,
            Finished,
        }
        #endregion
    }
}