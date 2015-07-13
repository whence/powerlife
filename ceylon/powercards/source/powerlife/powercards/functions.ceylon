import ceylon.math.float {
    random
}

"http://stackoverflow.com/questions/20486670/ceylon-equivalent-of-collections-shuffle"
shared Element[] shuffleSeq<Element>({Element*} elements) {
    value shuffled = Array(elements);
    for (index->element in shuffled.indexed) {
        value randomIndex = (random() * (index + 1)).integer;
        if (randomIndex != index) {
            assert (exists randomElement = shuffled[randomIndex]);
            shuffled.set(index, randomElement);
            shuffled.set(randomIndex, element);
        }
    }
    return shuffled.sequence();
}
