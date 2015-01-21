package powercards;

public class Choice {
  private final String name;
  private final boolean selectable;

  public Choice(String name, boolean selectable) {
    this.name = name;
    this.selectable = selectable;
  }

  public String getName() {
    return name;
  }

  public boolean isSelectable() {
    return selectable;
  }
}
