$(function() {
    var Todo = Backbone.Model.extend({
        defaults: function() {
            return {
                title: "empty todo...",
                order: 1,
                done: false
            };
        },

        toggle: function() {
            this.save({done: !this.get("done")});
        }
    });
});
