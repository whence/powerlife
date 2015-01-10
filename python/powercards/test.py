import unittest
import app
from cardlib import Copper, Estate, Remodel, Card, ActionCard, TreasureCard, VictoryCard

class GameSetupTests(unittest.TestCase):
    def test_game_setup(self):
        game = app.Game(['wes', 'bec'])
        self.assertEqual(len(game.players), 2)
        self.assertEqual(sorted([player.name for player in game.players]), ['bec', 'wes'])

        active_player = game.active_player()
        for player in game.players:
            self.assertEqual(len(player.deck), 5)
            self.assertEqual(len(player.hand), 5)
            self.assertEqual(len(player.played), 0)
            self.assertEqual(len(player.discard), 0)

            fulldeck = player.deck + player.hand
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, Estate)), 3)
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, Copper)), 7)
            self.assertFalse(any(isinstance(c, ActionCard) for c in player.hand))

            if player == active_player:
                self.assertEqual((player.actions, player.buys, player.coins), (1, 0, 0))
            else:
                self.assertEqual((player.actions, player.buys, player.coins), (0, 0, 0))

        self.assertEqual(len(game.board.trash), 0)
        self.assertEqual(game.stage, 'action')

class CardlibTests(unittest.TestCase):
    def test_card_type(self):
        self.assertIsInstance(Copper(), TreasureCard)
        self.assertIsInstance(Estate(), VictoryCard)
        self.assertIsInstance(Remodel(), ActionCard)

    def test_all_inherif_from_card(self):
        for card in [Copper(), Estate(), Remodel()]:
            self.assertIsInstance(card, Card)

class GamePlayTests(unittest.TestCase):
    def test_skip_to_treasure(self):
        game = app.Game(['wes', 'bec'])
        app.play(game)
        self.assertEqual(game.stage, 'treasure')

if __name__ == '__main__':
    unittest.main()
