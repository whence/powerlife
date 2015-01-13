import random
from player import Player
from board import Board
import cardlib

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

    def discard_deck(self, player):
        player.discard.extend(player.deck[::-1])
        player.deck = []

    def putback_many_to_deck(self, src, indexes, dst):
        return self.move_many(src, indexes, dst)

    def trash_one(self, src, index):
        return self.move_one(src, index, self.board.trash)

    def gain_one(self, pile, dst):
        card = pile.pop
        dst.append(card)
        return card

    def play_action(self):
        if self.active_player().actions == 0:
            self.output('no more actions, skip to treasure stage')
            self.stage = 'treasure'
        else:
            action_i = self.choose_optional_one(
                message='select an action card to play',
                items=[(card.name, isinstance(card, cardlib.ActionCard)) for card in self.active_player().hand])
            if action_i is None:
                self.output('skip to treasure stage')
                self.stage = 'treasure'
            else:
                self.output('you have chosen {0}'.format(action_i))

    def play_treasure(self):
        treasure_i = self.choose_unlimited(
            message='select treasure cards to play',
            items=[(card.name, isinstance(card, cardlib.TreasureCard)) for card in self.active_player().hand])
        if treasure_i is None:
            self.output('skip to buy stage')
            self.stage = 'buy'
        else:
            self.output('you have chosen {0}'.format(treasure_i))

    def play_buy(self):
        pass

    def play_cleanup(self):
        pass

    def play(self):
        if self.stage == 'action':
            self.play_action()
        elif self.stage == 'treasure':
            self.play_treasure()
        elif self.stage == 'buy':
            self.play_buy()
        elif self.stage == 'cleanup':
            self.play_cleanup()

    def choose_optional_one(self, message, items):
        if any(selectable for _, selectable in items):
            while True:
                self.output(message)
                self.output_items(items)
                raw = self.input()
                if raw == 'skip':
                    return None
                elif items[int(raw)][1]:
                    return int(raw)
                else:
                    self.output('{} is not selectable'.format(items[int(raw)][0]))
        else:
            return None

    def choose_one(self, message, items):
        if any(selectable for _, selectable in items):
            while True:
                self.output(message)
                self.output_items(items)
                raw = self.input()
                if items[int(raw)][1]:
                    return int(raw)
                else:
                    self.output('{} is not selectable'.format(items[int(raw)][0]))
        else:
            return None

    def choose_unlimited(self, message, items):
        if any(selectable for _, selectable in items):
            while True:
                self.output(message)
                self.output_items(items)
                raw = self.input()
                if raw == 'skip':
                    return None
                else:
                    idx = [int(index) for index in raw.split(',') if index.strip()]
                    if all(items[i][1] for i in idx):
                        return idx
                    else:
                        self.output('some items are not selectable')
        else:
            return None

    def output_items(self, items):
        for i, (name, selectable) in enumerate(items):
            self.output('[{}] {} {}'.format(i, name, '(select)' if selectable else ''))

    def output(self, message):
        print(message)

    def input(self):
        return raw_input('>')
