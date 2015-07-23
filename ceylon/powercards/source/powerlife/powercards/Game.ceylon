import powerlife.powercards.stages {
    actionStage
}
shared class Game(shared [Player&Interactive+] players) {
    shared Player&Interactive active => players[0];
    variable Stage stage = actionStage;
    
    shared void playOne() {
        stage = stage.play(this);
    }
}
