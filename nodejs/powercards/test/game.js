var _ = require('lodash');
var util = require('util');
var createGame = require('../lib/game').createGame;

exports.gameCreated = function(test) {
    var playerCount = 4;
    var game = createGame(_.times(playerCount, function(n) { return util.format('player%d', n); }));

    test.strictEqual(game.players.length, playerCount);
    test.strictEqual(game.activePlayer, game.players[game.activePlayerIndex], 'active player is not indexed correctly');

    (function(player) {
        test.strictEqual(player.stage, 'action');
        test.strictEqual(player.stat.actions, 1);
        test.strictEqual(player.stat.buys, 1);
        test.strictEqual(player.stat.coins, 0);
    })(game.activePlayer);

    var inactivePlayers = _.filter(game.players, function(p) { return p !== game.activePlayer; });
    _.each(inactivePlayers, function(player) {
        test.strictEqual(player.stage, 'inactive');
        test.strictEqual(player.stat.actions, 0);
        test.strictEqual(player.stat.buys, 0);
        test.strictEqual(player.stat.coins, 0);
    });

    testPlayerCardZones(game, test);

    test.done();
};

var testPlayerCardZones = function(game, test) {
    _.each(game.players, function(player) {
        _.each(['deck', 'hand', 'played', 'discard'], function(cardzone) {
            test.ok(_.has(player, cardzone), util.format('%s does not have %s', player.name, cardzone));
        });
    });
};
