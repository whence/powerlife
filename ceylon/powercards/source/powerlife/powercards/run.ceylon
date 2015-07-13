import powerlife.powercards.cards {
    Island,
    Silver,
    Estate
}
shared void run() {
    Card[] cards = [Island(), Silver(), Estate()];
    value a = [for (c in cards) if (is BasicVictoryCard&ActionCard c) c];
    print(a);
}
