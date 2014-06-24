class Card:
    pass

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

    def play(game):
        def trash(index):
            trashed_card = game.active.move_one(index, game.active.hand, lambda cards: game.active.to_trash(cards, game.board))
            print('trashed {}'.format(trashed_card.name))
            game.active.choose(
                message='select a pile to gain',
                items=game.board.piles,
                each=lambda pile: not pile.empty and pile.sample.costs <= trashed_card.costs + 2,
                limit='one',
                commands={
                    'unable': lambda: print('no pile available to gain'),
                    'gain': gain
                }
            )

        def gain(index):
            gained_card = game.active.gain_from_pile(game.board.piles[index])
            print('gained {}'.format(gained_card.name))

        game.active.choose(
            message='select a card to remodel',
            items=game.active.hand,
            each=lambda card: isinstance(card, Card),
            limit='one',
            commands={
                'unable': lambda: print('no card in hand to remodel'),
                'trash': trash
            }
        )
