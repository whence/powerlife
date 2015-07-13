import powerlife.powercards {
    Stage,
    Game,
    Item
}
import powerlife.powercards.choices {
    One,
    nonSelectable
}
shared object actionStage satisfies Stage {
    shared actual Stage play(Game game) {
        value chooseOne = game.active.chooseOne("hello", [Item()]);
        switch (chooseOne)
        case (nonSelectable) {
        }
        case (is One) {
        }
        return treasureStage;
    }
}
