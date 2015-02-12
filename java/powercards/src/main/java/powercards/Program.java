package powercards;

import java.util.Arrays;

public class Program {
  public static void main(String[] args){
    InputOutput inout = new ConsoleInputOutput();
    Dialog dialog = new Dialog(inout);
    Game game = new Game(Arrays.asList("wes", "bec"), dialog);
    game.play();
  }
}
