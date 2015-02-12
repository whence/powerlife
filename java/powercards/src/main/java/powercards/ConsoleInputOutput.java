package powercards;

import java.io.Console;

public class ConsoleInputOutput implements InputOutput {
  private Console console;

  public ConsoleInputOutput() {
    this.console = System.console();
  }

  @Override
  public String input() {
    return console.readLine();
  }

  @Override
  public void output(String message) {
    System.out.println(message);
  }
}
