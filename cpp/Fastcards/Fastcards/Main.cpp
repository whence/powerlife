#include <vector>
#include <type_traits>
#include "Game.h"
#include "Player.h"

int main()
{
	const char* player_names[] = { "wes", "bec" };
	std::vector<Player*> players;
	auto player_count = std::extent<decltype(player_names)>::value;
	players.reserve(player_count);
	for (const auto& player_name : player_names)
	{
		players.push_back(new Player(player_name));
	}

	Game game(players);
	game.game_loop();

	for (auto player : players)
	{
		delete player;
	}

	return EXIT_SUCCESS;
}