import unittest
from turn import Turn
from player import Player
from board import Board
from cardlib import Copper, Estate, Card, ActionCard, TreasureCard, VictoryCard


class TurnTests(unittest.TestCase):
    def test_turn_init(self):
        turn = Turn(['wes', 'bec'])
        self.assertEqual((turn.actions, turn.buys, turn.coins), (1, 0, 0))
        self.assertFalse(any(isinstance(c, ActionCard) for c in turn.active.hand))


class PlayerTests(unittest.TestCase):
    def test_player_init(self):
        player = Player('wes')
        self.assertEqual(len(player.deck), 5)
        self.assertEqual(len(player.hand), 5)
        self.assertEqual(len(player.played), 0)
        self.assertEqual(len(player.discard), 0)

        fulldeck = player.deck + player.hand
        self.assertEqual(sum(1 for c in fulldeck if isinstance(c, Estate)), 3)
        self.assertEqual(sum(1 for c in fulldeck if isinstance(c, Copper)), 7)


class CardlibTests(unittest.TestCase):
    def test_card_type(self):
        self.assertIsInstance(Copper(), TreasureCard)
        self.assertIsInstance(Estate(), VictoryCard)

    def test_all_inherif_from_card(self):
        for card in [Copper(), Estate()]:
            self.assertIsInstance(card, Card)


class BoardTests(unittest.TestCase):
    def test_board_init(self):
        board = Board([])
        self.assertEqual(len(board.trash), 0)

if __name__ == '__main__':
    unittest.main()
