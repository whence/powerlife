var _ = require('lodash');
var cardlib = require('./cardlib');

function createGame(playerNames) {
    var game = {
        players: _.map(playerNames, createPlayer),
        trash: [],
    };

    activatePlayer(_.random(game.players.length - 1), game);

    return game;
}

function activatePlayer(playerIndex, game) {
    if (game.activePlayer) {
        game.activePlayer.stage = 'inactive';
        game.activePlayer.stat.actions = 0;
        game.activePlayer.stat.buys = 0;
        game.activePlayer.stat.coins = 0;
    }

    game.activePlayerIndex = playerIndex;
    game.activePlayer = game.players[playerIndex];

    game.activePlayer.stage = 'action';
    game.activePlayer.stat.actions = 1;
    game.activePlayer.stat.buys = 1;
    game.activePlayer.stat.coins = 0;
}

function createPlayer(playerName) {
    var estates = _.times(3, function() { return cardlib.estate; });
    var coppers = _.times(7, function() { return cardlib.copper; });
    var deck = _.shuffle(estates.concat(coppers));

    return {
        name: playerName,
        deck: _.take(deck, 5),
        hand: _.drop(deck, 5),
        played: [],
        discard: [],
        stage: 'inactive',
        stat: { actions: 0, buys: 0, coins: 0 },
    };
}

exports.createGame = createGame;
