package core

class Zone[A] {
  private[this] val buffer = new collection.mutable.ArrayBuffer[A]

  def transferToEnd(fromIndex: Int, to: Zone[A]): A = {
    val elem = buffer.remove(fromIndex)
    to.append(elem)
    elem
  }

  def transferMultiToEnd(fromIndexes: Vector[Int], to: Zone[A]): Vector[A] = {
    val elems = fromIndexes.map(index => buffer(index))
    fromIndexes.sorted.zipWithIndex.foreach {
      case (index, offset) =>
        buffer.remove(index - offset)
    }
    to.appendAll(elems)
    elems
  }

  def transferAllToEnd(to: Zone[A]) {
    to.appendAll(buffer)
    buffer.clear()
  }

  def shuffle() {
    val elems = util.Random.shuffle(buffer)
    buffer.clear()
    appendAll(elems)
  }

  def append(elem: A) {
    buffer += elem
  }

  def appendAll(elems: Seq[A]) {
    buffer ++= elems
  }

  def remove(fromIndex: Int): A =  buffer.remove(fromIndex)

  def isEmpty: Boolean = buffer.isEmpty

  def toVector: Vector[A] = buffer.toVector
}