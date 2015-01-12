import dialog
import random

class Game(object):
    def __init__(self, player_names):
        assert 2 <= len(player_names) <= 4, 'only 2 to 4 players allowed'
        
        self.players = [Player(name) for name in player_names]
        self.active_index = random.randint(0, len(self.players)-1)
        self.board = Board([(Copper, 60), (Estate, 12)])
        self.stage = 'action'

        self.active_player().actions = 1

    def active_player(self):
        return self.players[self.active_index]

class Player(object):
    def __init__(self, name):
        self.name = name
        deck = [Estate() for i in range(3)] + [Copper() for i in range(7)]
        random.shuffle(deck)
        self.deck, self.hand = deck[:5], deck[5:]
        self.played, self.discard = [], []
        self.actions, self.buys, self.coins = 0, 0, 0

class Board(object):
    def __init__(self, piles):
        self.trash = []
        self.piles = piles

class Pile(object):
    def __init__(self, factory, count):
        self.factory = factory
        self.remaining = count
        self.sample = self.factory()

    def empty(self):
        return self.remaining <= 0

    def push(self):
        self.remaining += 1

    def pop(self):
        assert not self.empty, '{} pile is empty'.format(self.sample.name)
        self.remaining -= 1
        return self.factory()

    def name(self):
        return self.sample.name

def move_one(src, index, dst):
    card = src[index]
    del src[index]
    dst.append(card)
    return card

def move_many(src, indexes, dst):
    cards = [src[i] for i in indexes]
    cards_rev = [card for i, card in enumerate(src) if i not in indexes]
    src.clear()
    src.extend(cards_rev)
    dst.extend(cards)
    return cards

def discard_deck(player):
    player.discard.extend(player.deck[::-1])
    player.deck = []

def putback_many_to_deck(src, indexes, dst):
    return move_many(src, indexes, dst)

def trash_one(src, index, game):
    return move_one(src, index, game.board.trash)

def gain_one(pile, dst):
    card = pile.pop
    dst.append(card)
    return card

def play_action(game):
    if game.active_player().actions == 0:
        dialog.out('no more actions, skip to treasure stage')
        game.stage = 'treasure'
    else:
        chosen = dialog.optional_one(
            message='select an action card to play',
            items=[(card.name, isinstance(card, ActionCard)) for card in game.active_player().hand])
        if chosen is None:
            dialog.out('skip to treasure stage')
            game.stage = 'treasure'
        else:
            dialog.out('you have chosen {0}'.format(chosen))

def play_treasure(game):
    chosen = dialog.unlimited(
        message='select treasure cards to play',
        items=[(card.name, isinstance(card, TreasureCard)) for card in game.active_player().hand])
    if chosen is None:
        dialog.out('skip to buy stage')
        game.stage = 'buy'
    else:
        dialog.out('you have chosen {0}'.format(chosen))

def play_buy(game):
    pass

def play_cleanup(game):
    pass

def play(game):
    if game.stage == 'action':
        play_action(game)
    elif game.stage == 'treasure':
        play_treasure(game)
    elif game.stage == 'buy':
        play_buy(game)
    elif game.stage == 'cleanup':
        play_cleanup(game)

class Card(object):
    def name(self):
        return type(self).__name__

class ActionCard(Card):
    pass

class TreasureCard(Card):
    pass

class VictoryCard(Card):
    pass

class Copper(TreasureCard):
    pass

class Estate(VictoryCard):
    pass

class Remodel(ActionCard):
    costs = 4

    def play(game):
        trash_i = dialog.one(
            message='select a card to remodel',
            items=[(card.name, True) for card in game.active.hand])
        if trash_i is None:
            dialog.out('no card in hand to remodel')
        else:
            trashed_card = trash_one(game.active_player().hand, trasn_i, game)
            dialog.out('trashed {}'.format(trashed_card.name))

            gain_i = dialog.one(
                message='select a pile to gain',
                items=[(pile.name, not pile.empty and pile.sample.costs <= trashed_card.costs + 2) for pile in game.board.piles])
            if gain_i is None:
                dialog.out('no pile available to gain')
            else:
                gained_card = gain_one(game.board.piles[gain_i], game.active_player().discard)
                dialog.out('gained {}'.format(gained_card.name))

