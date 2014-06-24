package powercards

object CardCasts {
  import Core._
  
  def toActionCard: PartialFunction[Card, Card with Actionable] = {
    case x: Actionable => x
  }
  
  def toActionCardWith(p: Card with Actionable => Boolean): PartialFunction[Card, Card with Actionable] = {
    case x: Actionable if p(x) => x
  }
  
  def toTreasureCard: PartialFunction[Card, Card with Treasurable] = {
    case x: Treasurable => x
  }
  
  def toTreasureCardWith(p: Card with Treasurable => Boolean): PartialFunction[Card, Card with Treasurable] = {
    case x: Treasurable if p(x) => x
  }
  
  def toVictoryCard: PartialFunction[Card, Card with Victoriable] = {
    case x: Victoriable => x
  }
  
  def toVictoryCardWith(p: Card with Victoriable => Boolean): PartialFunction[Card, Card with Victoriable] = {
    case x: Victoriable if p(x) => x
  }
  
  def toCard: PartialFunction[Card, Card] = {
    case x: Card => x
  }
  
  def toCardWith(p: Card => Boolean): PartialFunction[Card, Card] = {
    case x: Card if p(x) => x
  }
}