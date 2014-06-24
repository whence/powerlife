package core

import concurrent.Future

trait Card {
  def name: String
  def cost: Int
  override def toString: String = name
}

trait Actionable {
  def play: Game => Future[Game]
}

trait Treasurable {
  def coin: Int
}

trait Victoriable {
  def vp: Int
}

class BasicActionCard(
  val name: String,
  val cost: Int,
  val play: Game => Future[Game]
) extends Card with Actionable

class BasicTreasureCard(
  val name: String,
  val cost: Int,
  val coin: Int
) extends Card with Treasurable

class BasicVictoryCard(
  val name: String,
  val cost: Int,
  val vp: Int
) extends Card with Victoriable

object CardCasts {
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