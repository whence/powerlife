#pragma once

#include <vector>
#include <random>

class Player;
class Card;

class Game
{
private:
	Game(const Game&);
	Game& operator=(const Game&);
	std::vector<Player*> players;
	unsigned active_player_index;
	std::vector<Card*> trash;
	std::default_random_engine random_engine;
	void next_turn();

public:
	explicit Game(const std::vector<Player*>&);
	Player* active_player() const;
	void move_cards(const std::vector<Card*>&, std::vector<Card*>&, std::vector<Card*>&);
	void game_loop();
};

inline Player* Game::active_player() const
{
	return players[active_player_index];
}