package powercards

trait Logger {
  def output(message: String): Unit
}

object ConsoleLogger extends Logger {
  def output(message: String): Unit = println(message)
}

class SinkLogger extends Logger {
  val outputBuffer = collection.mutable.ArrayBuffer.empty[String]
  def output(message: String): Unit = outputBuffer.append(message)
}
