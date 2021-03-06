package core

class DispatchContext(val game: Game, channel: Channel) extends Receiver {
  private[this] val mailbox = collection.mutable.Queue.empty[(Receiver, Message)]
  private[this] var inputRequirer: Option[(Receiver, String)] = None
  private[this] val logBuffer = new StringBuilder()

  channel.subscribeInput(handleInput)

  def send(receiver: Receiver, message: Message) {
    mailbox.enqueue((receiver, message))
  }

  def receive(message: Message, context: DispatchContext) { message match {
    case InputRequired(dialogMessage, sender) =>
      inputRequirer = Some(sender, dialogMessage)
  }}

  def log(content: String) {
    logBuffer.append(content)
    logBuffer.append(Utils.newline)
  }

  private[this] def handleInput(input: String): (String, Boolean) = {
    inputRequirer match {
      case Some((receiver, _)) =>
        send(receiver, InputArrived(input))
      case None =>
    }
    inputRequirer = None
    processMailbox()

    val bufferOutput = logBuffer.toString()
    logBuffer.clear()

    inputRequirer match {
      case Some((_, dialogMessage)) =>
        (bufferOutput + dialogMessage, true)
      case None =>
        (bufferOutput, false)
    }
  }

  @annotation.tailrec
  private[this] final def processMailbox() {
    if (!inputRequirer.isDefined && !mailbox.isEmpty) {
      val (receiver, message) = mailbox.dequeue()
      receiver.receive(message, this)
      processMailbox()
    }
  }
}

trait Receiver {
  def receive(message: Message, context: DispatchContext)
}

trait Channel {
  def subscribeInput(handler: String => (String, Boolean))
}