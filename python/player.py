from cardlib import Copper, Estate
import random


class Player:
    def __init__(self, name):
        self.name = name
        deck = [Estate() for i in range(3)] + [Copper() for i in range(7)]
        random.shuffle(deck)
        self.deck, self.hand = deck[:5], deck[5:]
        self.played, self.discard = [], []

    def choose(self, message, items, each, limit, commands):
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
