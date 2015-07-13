shared interface Card {
    shared default String name => className(this);
    
    shared formal Integer cost;
    shared default Integer calculateCost(Game game) => cost;
}

shared interface ActionCard satisfies Card {
    shared formal void play(Game game);
}

shared interface TreasureCard satisfies Card {
    shared formal void play(Game game);
}

shared interface VictoryCard satisfies Card {
    shared formal Integer calculateVps(Card[] cards);
}

shared interface BasicTreasureCard satisfies TreasureCard {
    shared formal Integer coins;
    shared actual void play(Game game) {
        game.active.coins += coins;
    }
}

shared interface BasicVictoryCard satisfies VictoryCard {
    shared formal Integer vps;
    shared actual Integer calculateVps(Card[] cards) => vps;
}
