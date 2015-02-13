package powercards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class RecordedInputOutput implements InputOutput {
  private final Queue<String> inputQueue;
  private final List<String> outputBuffer;
  private final Queue<Consumer<List<Card>>> shuffleQueue;

  public RecordedInputOutput() {
    inputQueue = new LinkedList<>();
    outputBuffer = new ArrayList<>();
    shuffleQueue = new LinkedList<>();
  }

  public void queueInputs(String... inputs) {
    Collections.addAll(inputQueue, inputs);
  }

  public void queueShuffle(Consumer<List<Card>> consumer) {
    shuffleQueue.add(consumer);
  }

  public boolean hasOutputs(String... messages) {
    if (outputBuffer.isEmpty() || messages.length == 0) {
      return false;
    }

    int iBuffer = 0;
    int iMessage = 0;
    while (iBuffer < outputBuffer.size() && iMessage < messages.length) {
      if (outputBuffer.get(iBuffer).equals(messages[iMessage])) {
        iMessage++;
      }
      iBuffer++;
    }
    return iMessage == messages.length;
  }

  public boolean noOutput() {
    return outputBuffer.isEmpty();
  }

  public List<String> getOutputBuffer() {
    return outputBuffer;
  }

  public Queue<String> getInputQueue() {
    return inputQueue;
  }

  @Override
  public String input() {
    return inputQueue.remove();
  }

  @Override
  public void output(String message) {
    outputBuffer.add(message);
  }

  @Override
  public void shuffle(List<Card> cards) {
    shuffleQueue.remove().accept(cards);
  }
}
