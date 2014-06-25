#pragma once

#include <random>

class Card;
class Game;

class Player
{
public:
	class PlayerStat
	{
	public:
		int actions;
		int buys;
		int coins;

		PlayerStat();
	};

	enum PlayerStage
	{
		inactive,
		action,
		treasure,
		buy,
		cleanup
	};

private:
	Player(const Player&);
	Player& operator=(const Player&);
	std::string name;
	PlayerStat stat;
	std::vector<Card*> deck;
	std::vector<Card*> hand;
	std::vector<Card*> played;
	std::vector<Card*> discard;
	std::default_random_engine random_engine;

public:
	PlayerStage stage;
	explicit Player(const std::string&);
	void activate();
	void deactivate();
	int draw_cards(int);
	void turn_action(Game*);
	void turn_treasure(Game*);
	void turn_buy(Game*);
	void turn_cleanup(Game*);
};