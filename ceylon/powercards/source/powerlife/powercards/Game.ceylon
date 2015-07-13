import powerlife.powercards.stages {
    actionStage
}
shared class Game(shared [Player+] players) {
    shared Player active => players[0];
    variable Stage stage = actionStage;
    
    shared void playOne() {
        stage = stage.play(this);
    }
}
