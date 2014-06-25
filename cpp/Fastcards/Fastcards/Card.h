#pragma once

#include <functional>

class Game;

class Card
{
private:
	Card(const Card&);
    Card& operator=(const Card&);
	std::string name;

public:
	int cost;
	int coin;
	int vp;
	std::function<void(Game*)> action;
	
	Card(const std::string&, int);
	const std::string& to_s() const;
};

class Cards
{
private:
	Cards();
	Cards(const Cards&);
    Cards& operator=(const Cards&);

public:
	Card* copper;
	Card* silver;
	Card* gold;
	Card* estate;
	Card* duchy;
	Card* province;
	Card* remodel;
	Card* throne_room;

    static Cards& all();
};

namespace CardTests
{
	bool is_card(Card*);
	bool is_action(Card*);
	bool is_treasure(Card*);
	bool is_victory(Card*);
}

inline bool CardTests::is_card(Card* card)
{
	return true;
}

inline bool CardTests::is_action(Card* card)
{
	return card->action != nullptr;
}

inline bool CardTests::is_treasure(Card* card)
{ 
	return card->coin != -1;
}

inline bool CardTests::is_victory(Card* card)
{ 
	return card->vp != -1;
}