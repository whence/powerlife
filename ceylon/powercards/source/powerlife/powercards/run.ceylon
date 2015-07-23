import powerlife.powercards.cards {
    Island,
    Silver,
    Estate
}
shared void run() {
    Card[] cards = [Island(), Silver(), Estate()];
    value a = [for (c in cards) if (is BasicVictoryCard&ActionCard c) c];
    print(a);
    
    value player1 = ConsolePlayer("wes");
    object dummyConnection satisfies Connection {
    	shared actual String receive() => nothing;
    	shared actual void send(String message) {}
    }
    value player2 = NetworkPlayer("wes", dummyConnection);
    value game = Game([player1, player2]);
    value name = game.active.name;
}
