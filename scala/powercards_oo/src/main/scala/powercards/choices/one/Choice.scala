package powercards.choices.one

sealed abstract class Choice
case object NonSelectable extends Choice
case class One(index: Int) extends Choice
