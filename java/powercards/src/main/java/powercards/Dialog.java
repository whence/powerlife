package powercards;

import java.util.*;
import java.util.stream.IntStream;

public class Dialog {
  private final InputOutput inputOutput;

  public Dialog(InputOutput inputOutput) {
    this.inputOutput = inputOutput;
  }

  public InputOutput inout() {
    return inputOutput;
  }

  public OptionalInt chooseOne(String message, List<Choice> choices) {
    if (choices.stream().anyMatch(Choice::isSelectable)) {
      while (true) {
        inputOutput.output(message);
        outputChoices(choices);
        int index = Integer.parseInt(inputOutput.input());
        Choice choice = choices.get(index);
        if (choice.isSelectable()) {
          return OptionalInt.of(index);
        }

        inputOutput.output(String.format("%s is not selectable", choice.getName()));
      }
    }
    return OptionalInt.empty();
  }

  public OptionalInt chooseOptionalOne(String message, List<Choice> choices) {
    if (choices.stream().anyMatch(Choice::isSelectable)) {
      while (true) {
        inputOutput.output(message);
        outputChoices(choices);
        inputOutput.output("or skip");
        String input = inputOutput.input();
        if ("skip".equals(input)) {
          return OptionalInt.empty();
        }

        int index = Integer.parseInt(input);
        Choice choice = choices.get(index);
        if (choice.isSelectable()) {
          return OptionalInt.of(index);
        }

        inputOutput.output(String.format("%s is not selectable", choice.getName()));
      }
    }
    return OptionalInt.empty();
  }

  public Optional<int[]> chooseUnlimited(String message, List<Choice> choices) {
    if (choices.stream().anyMatch(Choice::isSelectable)) {
      while (true) {
        inputOutput.output(message);
        outputChoices(choices);
        inputOutput.output("or all, or skip");
        String input = inputOutput.input();
        if ("skip".equals(input)) {
          return Optional.empty();
        }

        if ("all".equals(input)) {
          return Optional.of(IntStream.range(0, choices.size()).filter(i -> choices.get(i).isSelectable()).toArray());
        }

        int[] indexes = Arrays.stream(input.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .mapToInt(Integer::parseInt).toArray();

        if (indexes.length == 0) {
          return Optional.empty();
        }

        if (Arrays.stream(indexes).allMatch(i -> choices.get(i).isSelectable())) {
          return Optional.of(indexes);
        }

        inputOutput.output("Some choices are not selectable");
      }
    }
    return Optional.empty();
  }

  private void outputChoices(List<Choice> choices) {
    for (int i = 0; i < choices.size(); i++) {
      Choice choice = choices.get(i);
      inputOutput.output(String.format("[%d] %s %s", i, choice.getName(),
          choice.isSelectable() ? "(select)" : ""));
    }
  }
}
