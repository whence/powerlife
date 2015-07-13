import ceylon.collection {
    Stack,
    LinkedList
}
class Pile(Card() cardFactory, Integer initialSize) {
    shared Card sample = cardFactory();
    shared variable Integer size = initialSize;
    Stack<Card> buffer = LinkedList<Card>();
    
    shared Boolean isEmpty => size <= 0;
    
    shared void push(Card card) {
        assert (card.name == sample.name);
        buffer.push(card);
        size += 1;
    }
    
    shared Card pop() {
        assert (!isEmpty);
        Card card = buffer.pop() else cardFactory();
        size -= 1;
        return card;
    }
}
