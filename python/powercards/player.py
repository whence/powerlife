import cardlib
import random

class Player(object):
    def __init__(self, name):
        self.name = name
        deck = [cardlib.Estate() for i in range(3)] + [cardlib.Copper() for i in range(7)]
        random.shuffle(deck)
        self.deck, self.hand = deck[:5], deck[5:]
        self.played, self.discard = [], []
        self.actions, self.buys, self.coins = 0, 0, 0
