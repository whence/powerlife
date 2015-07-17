package powercards

trait Logger {
  def output(message: String)
}

object ConsoleLogger extends Logger {
  def output(message: String) = println(message)
}

class SinkLogger extends Logger {
  val outputBuffer = collection.mutable.ArrayBuffer.empty[String]
  def output(message: String) = outputBuffer.append(message)
}
