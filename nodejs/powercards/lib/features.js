var _ = require('lodash');

var features = {
    card: {
        test: function() { return true; },
        testWith: function(pred) { return pred; },
    },
};

_.each(['action', 'treasure', 'victory'], function(feature) {
    features[feature] = {
        test: function(card) {
            return _.contains(card.features, features[feature]);
        },
        testWith: function(pred) {
            return function(card) {
                return features[feature].test(card) && pred(card);
            };
        },
    };
});

module.exports = features;
