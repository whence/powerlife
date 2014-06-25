#include <iostream>
#include <sstream>
#include <set>
#include <algorithm>
#include "Selection.h"
#include "Card.h"

template<typename T>
template<typename InputIt>
Selection<T>::Selection(InputIt first, InputIt last, std::function<bool(T)> pred)
	: items(first, last), selectable_indexes()
{
	for (size_t i = 0; i < items.size(); i++)
	{
		auto item = items[i];
		if (pred(item))
		{
			selectable_indexes.push_back(i);
		}
	}
}

template<typename T> 
size_t Selection<T>::selectable_size() const
{
	return selectable_indexes.size();
}

template<typename T> 
std::vector<T> Selection<T>::ask(const std::string& player_name, const std::string& message) const
{
	std::cout << player_name << ": " << message << std::endl;
	for (auto index : selectable_indexes)
	{
		std::cout << index << ": " << items[index]->to_s() << std::endl;
	}

	std::string input_line;
	std::getline(std::cin, input_line);
	if (input_line == "exit")
		std::exit(EXIT_SUCCESS);

	std::istringstream input_line_stream(input_line);
	std::set<int> selected_indexes((std::istream_iterator<int>(input_line_stream)), std::istream_iterator<int>());

	std::cout << "you have selected ";
	std::vector<T> selected_items;
	for (auto index : selectable_indexes)
	{
		if (selected_indexes.count(index) > 0)
		{
			auto selected_item = items[index];
			selected_items.push_back(selected_item);
			std::cout << selected_item->to_s() << ", ";
		}
	}
	std::cout << std::endl;

	return selected_items;
}

template class Selection<Card*>;
template Selection<Card*>::Selection(std::vector<Card*>::const_iterator, std::vector<Card*>::const_iterator, std::function<bool(Card*)>);