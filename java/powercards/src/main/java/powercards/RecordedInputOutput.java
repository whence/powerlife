package powercards;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RecordedInputOutput implements InputOutput {
  private final Queue<String> inputQueue;
  private final List<String> outputBuffer;

  public RecordedInputOutput() {
    inputQueue = new LinkedList<>();
    outputBuffer = new ArrayList<>();
  }

  public Queue<String> getInputQueue() {
    return inputQueue;
  }

  public List<String> getOutputBuffer() {
    return outputBuffer;
  }

  public boolean hasOutputs(List<String> messages) {
    return true; // TODO: implement and test
  }

  @Override
  public String input() {
    return inputQueue.remove();
  }

  @Override
  public void output(String message) {
    outputBuffer.add(message);
  }
}
