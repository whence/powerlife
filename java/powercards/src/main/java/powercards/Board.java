package powercards;

import java.util.ArrayList;
import java.util.List;

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

  public Pile getPile(Class<? extends Card> type) {
    return piles.stream().filter(p -> type.isInstance(p.getSample())).findAny().get();
  }

  public List<Card> getTrash() {
    return trash;
  }
}
