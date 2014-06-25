#include <iostream>
#include <string>
#include "Card.h"
#include "Game.h"

Card::Card(const std::string& card_name, int card_cost)
	: name(card_name), cost(card_cost), coin(-1), vp(-1), action()
{
}

const std::string& Card::to_s() const
{
	return name;
}

void play_remodel(Game* game)
{
	std::cout << "playing " << Cards::all().remodel->to_s() << std::endl;
}

void play_throne_room(Game* game)
{
	std::cout << "playing " << Cards::all().throne_room->to_s() << std::endl;
}

Cards::Cards()
{
	copper = new Card("Copper", 0); copper->coin = 1;
	silver = new Card("Silver", 3); silver->coin = 2;
	gold = new Card("Gold", 6); gold->coin = 3;

	estate = new Card("Estate", 2); estate->vp = 1;
	duchy = new Card("Duchy", 5); duchy->vp = 3;
	province = new Card("Province", 8); province->vp = 6;

	remodel = new Card("Remodel", 4); remodel->action = play_remodel;
	throne_room = new Card("Throne Room", 4); throne_room->action = play_throne_room;
}

Cards& Cards::all()
{
	static Cards instance;
    return instance;
}