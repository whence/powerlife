package core

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

object Selector {
  trait Requirement
  case object Unlimited extends Requirement
  case object MandatoryOne extends Requirement
  case object OptionalOne extends Requirement

  def select[A, B](
      items: Vector[A],
      collector: PartialFunction[A, B],
      requirement: Selector.Requirement,
      message: String
    ): Future[(Vector[B], Vector[A])] = {
    
    def parseInput(line: String): Vector[Int] =
      line.split(',').toVector.map(_.trim).filterNot(_.isEmpty).map(_.toInt)
        .distinct.filter { index => index < items.length && collector.isDefinedAt(items(index)) }
    
    def divide(indexes: Vector[Int]): (Vector[B], Vector[A]) = {
      val selected = indexes.map(items(_)).map(collector)
      val unselected = (0 until items.length).diff(indexes).map(items(_)).toVector
      (selected, unselected)
    }

    if (items.exists(collector.isDefinedAt)) {
	  println(message)
	  items.zipWithIndex filter {
	    case (item, index) => collector.isDefinedAt(item)
	  } foreach { case(item, index) =>
	    printf("%d: %s", index, item)
	    println()
	  }
	  Future { readLine() } flatMap { line =>
	      val indexes = parseInput(line)
	      requirement match {
	        case MandatoryOne if indexes.length == 1 =>
	          Future.successful(divide(indexes))
	        case MandatoryOne =>
	          println("you must select one item")
	          select(items, collector, requirement, message)
	        case OptionalOne if indexes.length < 2 =>
	          Future.successful(divide(indexes))
	        case OptionalOne =>
	          println("you cannot select more than one item")
	          select(items, collector, requirement, message)
	        case Unlimited =>
	          Future.successful(divide(indexes))
	      }
	  }
    } else {
      Future.successful((Vector.empty[B], items))
    }
  }
}