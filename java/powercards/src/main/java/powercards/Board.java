package powercards;

import java.util.ArrayList;
import java.util.List;

public class Board {
  private final List<Pile> piles;
  private final List<Card> trash;

  public Board(List<Pile> piles) {
    this.piles = piles;
    this.trash = new ArrayList<>();
  }

  public List<Pile> getPiles() {
    return piles;
  }

  public List<Card> getTrash() {
    return trash;
  }
}
