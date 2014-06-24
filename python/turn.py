import random
from player import Player
from board import Board
from cardlib import Copper, Estate, ActionCard, TreasureCard


class Turn:
    def __init__(self, player_names):
        assert 2 <= len(player_names) <= 4, 'only 2 to 4 players allowed'
        self.players = [Player(name) for name in player_names]
        active_index = random.randint(0, len(self.players)-1)
        self.players = self.players[active_index:] + self.players[:active_index]
        self.active = self.players[0]
        self.board = Board([(Copper, 60), (Estate, 12)])
        self.actions, self.buys, self.coins = 1, 0, 0
        self.stage = 'action'

    def start(self):
        while True:
            if self.stage == 'action':
                if self.actions == 0:
                    print('no more actions, skip to treasure stage')
                    self.stage = 'treasure'
                else:
                    chosen = self.active.choose(
                        message='select an action card to play',
                        items=self.active.hand,
                        each=lambda c: isinstance(c, ActionCard),
                        limit='optional_one')
                    if chosen == 'not_selectable':
                        print('no action card, skip to treasure stage')
                        self.stage = 'treasure'
                    elif chosen == 'skip':
                        print('you have chosen to skip action stage')
                        self.stage = 'treasure'
                    else:
                        print('you have chosen {0}'.format(chosen))

            elif self.stage == 'treasure':
                chosen = self.active.choose(
                    message='select treasure cards to play',
                    items=self.active.hand,
                    each=lambda c: isinstance(c, TreasureCard),
                    limit='unlimited')
                if chosen == 'not_selectable':
                    print('no treasure card, skip to buy stage')
                    self.stage = 'buy'
                elif chosen == 'skip':
                    print('you have chosen to skip treasure stage')
                    self.stage = 'buy'
                else:
                    print('you have chosen {0}'.format(chosen))

            elif self.stage == 'buy':
                print('buy stage not implemented')
                break

            else:
                break
