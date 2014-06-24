var util = require('util');
var createGame = require('./lib/game').createGame;

var game = createGame(['wes', 'bec']);
console.log(util.inspect(game.activePlayer, { colors: true }));

/*
function gameloop() {

}

gameloop.then(nexttick(gameloop).end();
*/
