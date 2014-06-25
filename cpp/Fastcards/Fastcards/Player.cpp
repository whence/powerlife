#include <vector>
#include <algorithm>
#include <numeric>
#include <iterator>
#include <ostream>
#include <ctime>
#include "Player.h"
#include "Card.h"
#include "Selection.h"
#include "Game.h"

Player::PlayerStat::PlayerStat()
	: actions(0), buys(0), coins(0)
{
}

Player::Player(const std::string& player_name)
	: name(player_name), stat(), stage(PlayerStage::inactive), deck(), hand(), played(), discard(), random_engine((unsigned)std::time(0))
{
	std::vector<Card*> deck_v;
	deck_v.reserve(10);
	deck_v.insert(deck_v.cend(), 3, Cards::all().estate);
	deck_v.insert(deck_v.cend(), 7, Cards::all().copper);
	std::shuffle(deck_v.begin(), deck_v.end(), random_engine);
	deck.insert(deck.cend(), deck_v.cbegin(), deck_v.cbegin() + 5);
	hand.insert(hand.cend(), deck_v.cbegin() + 5, deck_v.cend());
}

void Player::activate()
{
	stage = PlayerStage::action;
	stat.actions = 1;
	stat.buys = 1;
	stat.coins = 0;
}

void Player::deactivate()
{
	stage = PlayerStage::inactive;
	stat.actions = 0;
	stat.buys = 0;
	stat.coins = 0;
}

int Player::draw_cards(int count)
{
	while (count > 0)
	{
		if (deck.empty())
		{
			if (discard.empty())
				return count;

			deck.insert(deck.cend(), discard.cbegin(), discard.cend());
			discard.clear();
			std::shuffle(deck.begin(), deck.end(), random_engine);
		}
		else
		{
			auto card = deck.back();
			deck.pop_back();
			hand.push_back(card);
			count--;
		}
	}
	return 0;
}

void Player::turn_action(Game* game)
{
	Selection<Card*> action_selection(hand.cbegin(), hand.cend(), CardTests::is_action);
	if (action_selection.selectable_size() > 0)
	{
		auto action_cards = action_selection.ask(name, "choose an action card to play");
		if (!action_cards.empty())
		{
			game->move_cards(action_cards, hand, played);
			action_cards.front()->action(game);
		}
		else
		{
			stage = PlayerStage::treasure;
		}
	}
	else
	{
		stage = PlayerStage::treasure;
	}
}

void Player::turn_treasure(Game* game)
{
	Selection<Card*> treasure_selection(hand.cbegin(), hand.cend(), CardTests::is_treasure);
	if (treasure_selection.selectable_size() > 0)
	{
		auto treasure_cards = treasure_selection.ask(name, "choose treasure cards to play");
		if (!treasure_cards.empty())
		{
			game->move_cards(treasure_cards, hand, played);

			for (auto card : treasure_cards)
			{
				stat.coins += card->coin;
			}
		}
		else
		{
			stage = PlayerStage::buy;
		}
	}
	else
	{
		stage = PlayerStage::buy;
	}
}

void Player::turn_buy(Game* game)
{
	stage = PlayerStage::cleanup;
}

void Player::turn_cleanup(Game* game)
{
	discard.insert(discard.cend(), hand.cbegin(), hand.cend());
	hand.clear();

	discard.insert(discard.cend(), played.cbegin(), played.cend());
	played.clear();

	draw_cards(5);
	deactivate();
}