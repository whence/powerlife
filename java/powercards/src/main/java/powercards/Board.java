package powercards;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Board {
  private final List<Pile> piles;
  private final List<Card> trash;

  public Board(List<Pile> piles) {
    this.piles = new ArrayList<>(piles);
    this.trash = new ArrayList<>();
  }

  public List<Pile> getPiles() {
    return piles;
  }

  public Pile getPile(Predicate<Pile> predicate) {
    return piles.stream().filter(predicate).findAny().get();
  }

  public List<Card> getTrash() {
    return trash;
  }
}
