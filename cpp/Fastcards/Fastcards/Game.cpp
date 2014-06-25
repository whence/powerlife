#include <algorithm>
#include <ctime>
#include "Game.h"
#include "Player.h"

Game::Game(const std::vector<Player*>& source)
	: players(source.begin(), source.end()), active_player_index(0), trash(), random_engine((unsigned)std::time(0))
{
	std::uniform_int_distribution<int> dist(0, players.size() - 1);
	active_player_index = dist(random_engine);
}

void Game::next_turn()
{
	active_player_index++;
	if (active_player_index >= players.size())
		active_player_index = 0;
}

void Game::move_cards(const std::vector<Card*>& cards, std::vector<Card*>& source, std::vector<Card*>& target)
{
	for (const auto card : cards)
	{
		auto pos = std::find(source.cbegin(), source.cend(), card);
		if (pos != source.cend())
		{
			source.erase(pos);
			target.push_back(card);
		}
	}
}

void Game::game_loop()
{
	while (true)
	{
		switch (active_player()->stage)
		{
		case Player::PlayerStage::inactive:
			active_player()->activate();
			break;

		case Player::PlayerStage::action:
			active_player()->turn_action(this);
			break;

		case Player::PlayerStage::treasure:
			active_player()->turn_treasure(this);
			break;

		case Player::PlayerStage::buy:
			active_player()->turn_buy(this);
			break;

		case Player::PlayerStage::cleanup:
			active_player()->turn_cleanup(this);
			next_turn();
			break;
		}
	}
}