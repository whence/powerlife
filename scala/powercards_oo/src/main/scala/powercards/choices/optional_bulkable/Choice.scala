package powercards.choices.optional_bulkable

sealed abstract class Choice
case object NonSelectable extends Choice
case object Skip extends Choice
case class One(index: Int) extends Choice
case class Bulk(indexes: Vector[Int]) extends Choice
