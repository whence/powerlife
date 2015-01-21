package powercards;

import java.io.Console;

public class InputOutput {
  private Console console;

  public InputOutput() {
    this.console = System.console();
  }

  public String input() {
    return console.readLine();
  }

  public void output(String message) {
    System.out.println(message);
  }
}
