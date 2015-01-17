package powercards;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dialog {
  public Optional<int[]> chooseUnlimited() {
    List<String> indexStrings = new ArrayList<>();
    int[] indexes = indexStrings.stream().mapToInt(Integer::parseInt).toArray();
    return Optional.of(indexes);
  }
}
