import powerlife.powercards.cards {
    Copper,
    Estate
}

shared class Player(shared String name) {
    value initialDeck = shuffleSeq(expand {
            { for (i in 1..7) Copper() },
            { for (i in 1..3) Estate() }
        });
    
    shared variable Card[] deck = initialDeck[0..4];
    shared variable Card[] hand = initialDeck[5..9];
    shared variable Card[] played = [];
    shared variable Card[] discard = [];
    
    shared variable Integer actions = 0;
    shared variable Integer buys = 0;
    shared variable Integer coins = 0;
}

shared class ConsolePlayer(String name) extends Player(name) satisfies ConsoleInteractive {
}

shared class NetworkPlayer(String name, connection) extends Player(name) satisfies NetworkInteractive {
	shared actual Connection connection;
}
