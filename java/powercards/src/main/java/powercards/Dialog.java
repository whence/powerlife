package powercards;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class Dialog {
  private final InputOutput inputOutput;

  public Dialog(InputOutput inputOutput) {
    this.inputOutput = inputOutput;
  }

  public OptionalInt chooseOne(String message, List<Choice> choices) {
    return OptionalInt.empty();
  }

  public Optional<int[]> chooseUnlimited() {
    // TODO: incomplete
    List<String> indexStrings = new ArrayList<>();
    int[] indexes = indexStrings.stream().mapToInt(Integer::parseInt).toArray();
    return Optional.of(indexes);
  }
}
