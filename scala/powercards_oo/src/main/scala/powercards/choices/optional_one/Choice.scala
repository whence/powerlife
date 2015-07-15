package powercards.choices.optional_one

sealed abstract class Choice
case object NonSelectable extends Choice
case object Skip extends Choice
case class One(index: Int) extends Choice
