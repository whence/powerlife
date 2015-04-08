$(function() {
    var Album = Backbone.Model.extend({
        defaults: function() {
            return {
                photos: []
            };
        },

        url: function() {
            return '/api/albums/' + this.get('path');
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

        events: {
            'click .select-album': 'select'
        },

        initialize: function() {
            this.listenTo(this.model, 'change', this.render);
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        select: function() {
            this.model.set({selected: true});
        }
    });

    var AlbumListView = Backbone.View.extend({
        tagName: 'ul',

        className: 'list-unstyled',

        initialize: function() {
            this.listenTo(this.collection, 'reset', this.render);
            this.listenTo(this.collection, 'change:selected', this.toggle);
        },

        render: function() {
            this.$el.empty();
            this.collection.each(function(model) {
                var view = new AlbumSummaryView({model: model});
                this.$el.append(view.render().el);
            }, this);
            return this;
        },

        toggle: function(obj, selected) {
            if (selected) {
                this.$el.hide();
            } else {
                this.$el.show();
            }
        }
    });

    var AlbumDetailView = Backbone.View.extend({
        tagName: 'div',

        template: _.template($('#album-detail-template').html()),

        events: {
            'click .close-album': 'unselect'
        },

        initialize: function() {
            this.listenTo(this.model, 'change', this.render);            
            this.listenTo(this.model, 'change:selected', this.closeOnUnselect);
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        unselect: function() {
            this.model.set({selected: false});
        },

        closeOnUnselect: function(obj, selected) {
            if (!selected) {
                this.remove();
            }
        }
    });

    var AppView = Backbone.View.extend({
        el: $('#lmserve-app'),

        initialize: function() {
            this.collection = new AlbumList();
            this.listenTo(this.collection, 'change:selected', this.addDetailViewForSelected);
            this.$el.append(new AlbumListView({collection: this.collection}).render().el);
            this.collection.fetch({reset: true});
        },

        addDetailViewForSelected: function(model, selected) {
            if (selected) {
                this.$el.append(new AlbumDetailView({model: model}).render().el);
                model.fetch();
            }
        }
    });

    new AppView();
});
