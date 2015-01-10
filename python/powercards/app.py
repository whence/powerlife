import cardlib
import random

class Game(object):
    def __init__(self, player_names):
        assert 2 <= len(player_names) <= 4, 'only 2 to 4 players allowed'
        
        self.players = [Player(name) for name in player_names]
        self.active_index = random.randint(0, len(self.players)-1)
        self.board = Board([(cardlib.Copper, 60), (cardlib.Estate, 12)])
        self.stage = 'action'

        self.active_player().actions = 1

    def active_player(self):
        return self.players[self.active_index]

    def out(self, message):
        print(message)

class Player(object):
    def __init__(self, name):
        self.name = name
        deck = [cardlib.Estate() for i in range(3)] + [cardlib.Copper() for i in range(7)]
        random.shuffle(deck)
        self.deck, self.hand = deck[:5], deck[5:]
        self.played, self.discard = [], []
        self.actions, self.buys, self.coins = 0, 0, 0

    def choose(self, message, items, each, limit):
        pass

    def move_one(self, src, index, dst):
        card = src[index]
        del src[index]
        dst.append(card)
        return card

    def move_many(self, src, indexes, dst):
        cards = [src[i] for i in indexes]
        cards_rev = [card for i, card in enumerate(src) if i not in indexes]
        src.clear()
        src.extend(cards_rev)
        dst.extend(cards)
        return cards

    def discard_deck(self):
        self.discard.extend(self.deck[::-1])
        self.deck = []

    def putback_many_to_deck(self, src, indexes, dst):
        return self.move_many(self, src, indexes, dst)

    def trash_one(self, src, index, dst):
        return self.move_one(self, src, index, dst)

    def gain_one(self, src, index, dst):
        return self.move_one(self, src, index, dst)

    def gain_from_pile(self, pile, dst):
        card = pile.pop
        dst.append(card)
        return card
    
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

class Board(object):
    def __init__(self, piles):
        self.trash = []
        self.piles = piles

def play(game):
    if game.stage == 'action':
        if game.active_player().actions == 0:
            game.out('no more actions, skip to treasure stage')
            game.stage = 'treasure'
        else:
            chosen = game.active_player().choose(
                message='select an action card to play',
                items=game.active_player().hand,
                each=lambda c: isinstance(c, cardlib.ActionCard),
                limit='optional_one')
            if chosen == 'not_selectable':
                game.out('no action card, skip to treasure stage')
                game.stage = 'treasure'
            elif chosen == 'skip':
                game.out('you have chosen to skip action stage')
                game.stage = 'treasure'
            else:
                game.out('you have chosen {0}'.format(chosen))

    elif game.stage == 'treasure':
        chosen = game.active_player().choose(
            message='select treasure cards to play',
            items=game.active_player().hand,
            each=lambda c: isinstance(c, cardlib.TreasureCard),
            limit='unlimited')
        if chosen == 'not_selectable':
            game.out('no treasure card, skip to buy stage')
            game.stage = 'buy'
        elif chosen == 'skip':
            game.out('you have chosen to skip treasure stage')
            game.stage = 'buy'
        else:
            game.out('you have chosen {0}'.format(chosen))

    elif game.stage == 'buy':
        game.out('buy stage not implemented')

    else:
        pass
