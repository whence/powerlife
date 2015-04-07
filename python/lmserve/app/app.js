$(function() {
    var Album = Backbone.Model.extend({
        defaults: function() {
            return {
                photos: []
            };
        }
    });

    var AlbumList = Backbone.Collection.extend({
        model: Album,
        
        url: '/api/albums',
        
        parse: function(response) {
            return response.albums;
        }
    });

    var AlbumSummaryView = Backbone.View.extend({
        tagName: 'li',
        
        template: _.template($('#album-summary-template').html()),

        initialize: function() {
            this.listenTo(this.model, 'change', this.render);
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },
    });

    var AlbumListView = Backbone.View.extend({
        tagName: 'ul',

        className: 'list-unstyled',

        initialize: function() {
            this.collection = new AlbumList();
            this.listenTo(this.collection, 'reset', this.render);
            this.collection.fetch({reset: true});
        },

        render: function() {
            this.$el.empty();
            this.collection.each(function(model) {
                var view = new AlbumSummaryView({model: model});
                this.$el.append(view.render().el);
            }, this);
        }
    });

    $('#lmserve-app').html(new AlbumListView().el);
});
