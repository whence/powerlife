package powercards

object Functions {
  def divides[A](items: Vector[A], indexes: Vector[Int]): (Vector[A], Vector[A]) = indexes match {
    case Seq() =>
      (Vector.empty, items)
    case Seq(i) =>
      val (x, y) = divide(items, i)
      (Vector(x), y)
    case _ =>
      val selected = indexes.map(items(_))
      val unselected = items.indices.diff(indexes).map(items(_)).toVector
      (selected, unselected)
  }

  def divide[A](items: Vector[A], index: Int): (A, Vector[A]) = {
    val selected = items(index)
    val unselected = items.patch(index, Nil, 1)
    (selected, unselected)
  }

  @annotation.tailrec
  def loopTil[A](f: () => Option[A]): A = f() match {
    case Some(x) => x
    case None => loopTil(f)
  }
}
