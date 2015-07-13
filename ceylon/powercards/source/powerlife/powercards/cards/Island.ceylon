import powerlife.powercards {
    ActionCard,
    BasicVictoryCard,
    Game
}

shared class Island() satisfies ActionCard & BasicVictoryCard {
    cost = 4;
    vps = 2;
    
    shared actual void play(Game game) {
    }
}
