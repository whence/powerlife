#pragma once

#include <vector>
#include <functional>

template<typename T> 
class Selection
{
private:
	Selection(const Selection&);
    Selection& operator=(const Selection&);
	std::vector<T> items;
	std::vector<int> selectable_indexes;

public:
	template<typename InputIt>
	Selection(InputIt, InputIt, std::function<bool(T)>);
	size_t selectable_size() const;
	std::vector<T> ask(const std::string&, const std::string&) const;
};