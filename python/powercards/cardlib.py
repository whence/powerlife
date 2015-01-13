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

    def play(self, game):
        trash_i = game.choose_one(
            message='select a card to remodel',
            items=[(card.name, True) for card in game.active_player().hand])
        if trash_i is None:
            game.output('no card in hand to remodel')
        else:
            trashed_card = game.trash_one(game.active_player().hand, trash_i, game)
            game.output('trashed {}'.format(trashed_card.name))

            gain_i = game.choose_one(
                message='select a pile to gain',
                items=[(pile.name, not pile.empty and pile.sample.costs <= trashed_card.costs + 2) for pile in game.board.piles])
            if gain_i is None:
                game.output('no pile available to gain')
            else:
                gained_card = game.gain_one(game.board.piles[gain_i], game.active_player().discard)
                game.output('gained {}'.format(gained_card.name))

