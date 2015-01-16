import unittest
import cardlib
from game import Game
from mock import MagicMock

class GameSetupTests(unittest.TestCase):
    def test_game_setup(self):
        game = Game(['wes', 'bec'])
        self.assertEqual(len(game.players), 2)
        self.assertEqual([player.name for player in game.players], ['wes', 'bec'])

        active_player = game.active_player()
        for player in game.players:
            self.assertEqual(len(player.deck), 5)
            self.assertEqual(len(player.hand), 5)
            self.assertEqual(len(player.played), 0)
            self.assertEqual(len(player.discard), 0)

            fulldeck = player.deck + player.hand
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, cardlib.Estate)), 3)
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, cardlib.Copper)), 7)
            self.assertFalse(any(isinstance(c, cardlib.ActionCard) for c in player.hand))

            if player == active_player:
                self.assertEqual((player.actions, player.buys, player.coins), (1, 0, 0))
            else:
                self.assertEqual((player.actions, player.buys, player.coins), (0, 0, 0))

        self.assertEqual(len(game.board.trash), 0)
        self.assertEqual(game.stage, 'action')

class CardTests(unittest.TestCase):
    def test_card_type(self):
        self.assertIsInstance(cardlib.Copper(), cardlib.TreasureCard)
        self.assertIsInstance(cardlib.Estate(), cardlib.VictoryCard)
        self.assertIsInstance(cardlib.Remodel(), cardlib.ActionCard)

class GameTests(unittest.TestCase):
    def test_skip_to_treasure(self):
        game = Game(['wes', 'bec'])
        game.output = MagicMock()
        game.play()
        self.assertEqual(game.stage, 'treasure')
        game.output.assert_any_call('skip to treasure stage')
        self.assertEqual(game.output.call_count, 1)

if __name__ == '__main__':
    unittest.main()
