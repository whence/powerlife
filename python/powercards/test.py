import unittest
import gameplay

class GameSetupTests(unittest.TestCase):
    def test_game_setup(self):
        game = gameplay.Game(['wes', 'bec'])
        self.assertEqual(len(game.players), 2)
        self.assertEqual(sorted([player.name for player in game.players]), ['bec', 'wes'])

        active_player = game.active_player()
        for player in game.players:
            self.assertEqual(len(player.deck), 5)
            self.assertEqual(len(player.hand), 5)
            self.assertEqual(len(player.played), 0)
            self.assertEqual(len(player.discard), 0)

            fulldeck = player.deck + player.hand
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, gameplay.Estate)), 3)
            self.assertEqual(sum(1 for c in fulldeck if isinstance(c, gameplay.Copper)), 7)
            self.assertFalse(any(isinstance(c, gameplay.ActionCard) for c in player.hand))

            if player == active_player:
                self.assertEqual((player.actions, player.buys, player.coins), (1, 0, 0))
            else:
                self.assertEqual((player.actions, player.buys, player.coins), (0, 0, 0))

        self.assertEqual(len(game.board.trash), 0)
        self.assertEqual(game.stage, 'action')

class CardTests(unittest.TestCase):
    def test_card_type(self):
        self.assertIsInstance(gameplay.Copper(), gameplay.TreasureCard)
        self.assertIsInstance(gameplay.Estate(), gameplay.VictoryCard)
        self.assertIsInstance(gameplay.Remodel(), gameplay.ActionCard)

class GamePlayTests(unittest.TestCase):
    def test_skip_to_treasure(self):
        game = gameplay.Game(['wes', 'bec'])
        gameplay.play(game)
        self.assertEqual(game.stage, 'treasure')

if __name__ == '__main__':
    unittest.main()
