var _ = require('lodash');
var util = require('util');
var cardlib = require('../lib/cardlib');
var features = require('../lib/features');

exports.cardFeatures = function(test) {
    _.each(cardlib, function(card) {
        test.ok(_.has(card, 'name'), util.format('%s does not have name property', util.inspect(card)));
        test.ok(_.has(card, 'cost'), util.format('%s does not have cost property', card.name));

        if (features.action.test(card)) {
            test.ok(_.has(card, 'play'), util.format('%s does not have play property', card.name));
            test.ok(_.isFunction(card.play), util.format('%s does not have play function', card.name));
        }

        if (features.treasure.test(card)) {
            test.ok(_.has(card, 'coins'), util.format('%s does not have coins property', card.name));
        }

        if (features.victory.test(card)) {
            test.ok(_.has(card, 'vps'), util.format('%s does not have vps property', card.name));
        }
    });

    test.done();
};
