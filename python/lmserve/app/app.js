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

        className: 'list-inline',

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
            'click img': function() { this.updateIndex(1); },
            'click .first': function() { this.updateIndex(-this.model.get('photos').length); },
            'click .prev-fast': function() { this.updateIndex(-10); },
            'click .prev-fast-fast': function() { this.updateIndex(-50); },
            'click .prev': function() { this.updateIndex(-1); },
            'click .next': function() { this.updateIndex(1); },
            'click .next-fast': function() { this.updateIndex(10); },
            'click .next-fast-fast': function() { this.updateIndex(50); },
            'click .last': function() { this.updateIndex(this.model.get('photos').length); },
            'click .close-album': 'unselect'
        },

        initialize: function() {
            this.photoIndex = 0;
            this.listenTo(this.model, 'change', this.render);            
            this.listenTo(this.model, 'change:selected', this.closeOnUnselect);
            this.$el.html(this.template());
            this.imageOdd = this.$('img.odd');
            this.imageEven = this.$('img.even');
            this.title = this.$('.title');
            this.lastButton = this.$('button.last');
            this.forwardButtons = this.$('button.forward');
            this.backwardButtons = this.$('button.backward');
        },

        updateIndex: function(delta) {
            var length = this.model.get('photos').length;
            var index = this.photoIndex;
            index += delta;

            if (index >= length) {
                index = length - 1;
            } else if (index < 0) {
                index = 0;
            }

            this.photoIndex = index;

            this.render();
        },

        render: function() {
            this.title.text(this.model.get('name') + ' ' + (this.photoIndex + 1));

            var imageCurrent = (this.photoIndex % 2 === 0 ? this.imageEven : this.imageOdd);
            var imageNext = (this.photoIndex % 2 === 0 ? this.imageOdd : this.imageEven);
            this.updateImage(imageCurrent, this.model.get('photos')[this.photoIndex]);
            imageCurrent.show();
            if (this.photoIndex + 1 < this.model.get('photos').length) {
                this.updateImage(imageNext, this.model.get('photos')[this.photoIndex + 1]);
            }
            imageNext.hide();

            this.lastButton.text(this.model.get('photos').length);
            this.forwardButtons.prop('disabled', this.photoIndex >= this.model.get('photos').length - 1);
            this.backwardButtons.prop('disabled', this.photoIndex <= 0);
            return this;
        },

        updateImage: function(image, src) {
            if (image.attr('src') !== src) {
                image.attr('src', src)
            }
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
