package powercards;

import java.util.List;

public interface InputOutput {
  String input();
  void output(String message);
  void shuffle(List<Card> cards);
}
