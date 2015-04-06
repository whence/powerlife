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

    var Albums = new AlbumList();

    var AlbumSummaryView = Backbone.View.extend({
        tagName: 'li',
        
        template: _.template($('#album-summary-template').html()),

        initialize: function() {
            this.listenTo(this.model, 'change', this.render);
            this.listenTo(this.model, 'destroy', this.remove);
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },
    });

    var AppView = Backbone.View.extend({
        el: $('#lmserve-app'),

        initialize: function() {
            this.listenTo(Albums, 'add', this.addOne);
            this.listenTo(Albums, 'reset', this.addAll);
            Albums.fetch({reset: true});
        },

        addOne: function(album) {
            var view = new AlbumSummaryView({model: album});
            this.$('#album-list').append(view.render().el);
        },

        addAll: function() {
            Albums.each(this.addOne, this);
        }
    });

    new AppView();
});
