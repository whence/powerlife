var _ = require('lodash');
var f = require('./features');

var cardlib = {
    copper: { cost: 0, coins: 1, features: [f.treasure] },
    silver: { cost: 3, coins: 2, features: [f.treasure] },
    gold: { cost: 6, coins: 3, features: [f.treasure] },

    estate: { cost: 2, vps: 1, features: [f.victory] },
    duchy: { cost: 5, vps: 3, features: [f.victory] },
    province: { cost: 8, vps: 6, features: [f.victory] },

    remodel: { cost: 4, play: play_remodel, features: [f.action]},
    throne_room: { cost: 4, play: play_throne_room, features: [f.action] },
};

_.each(cardlib, function(card, name) {
    card.name = name;
    card.inspect = function() { return name; };
});

function play_remodel() {
}

function play_throne_room() {
}

module.exports = cardlib;
