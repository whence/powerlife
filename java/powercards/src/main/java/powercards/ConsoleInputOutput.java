package powercards;

import java.io.Console;
import java.util.Collections;
import java.util.List;

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

  @Override
  public void shuffle(List<Card> cards) {
    Collections.shuffle(cards);
  }
}
