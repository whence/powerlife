package powercards

trait IO {
  def input(): String
  def output(message: String): Unit
}

object ConsoleIO extends IO {
  def input(): String = io.StdIn.readLine()
  def output(message: String) = println(message)
}

class RecordedIO(inputs: Seq[String]) extends IO {
  val inputStack = collection.mutable.Stack.concat(inputs)
  val outputBuffer = collection.mutable.ArrayBuffer.empty[String]

  def input(): String = inputStack.pop()
  def output(message: String) = outputBuffer.append(message)
}
