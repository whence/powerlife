package core

trait Card {
  def name: String
  def cost: Int
}

trait Actionable {
  def createSubReceiver(
     player: Player,
     card: Card with Actionable)
  : SubReceiver
}

trait Treasurable {
  def coin: Int
}

trait Victoriable {
  def vp: Int
}

object Estate extends Card with Victoriable {
  val name = "Estate"
  val cost = 2
  val vp = 1
}

object Copper extends Card with Treasurable {
  val name = "Copper"
  val cost = 0
  val coin = 1
}

object Remodel extends Card with Actionable {
  val name = "Remodel"
  val cost = 4
  def createSubReceiver(
     player: Player,
     card: Card with Actionable)
  : SubReceiver =
    new RemodelPlayer(player, this)
}

object ThroneRoom extends Card with Actionable {
  val name = "Throne Room"
  val cost = 4
  def createSubReceiver(
     player: Player,
     card: Card with Actionable)
  : SubReceiver =
    new ThroneRoomPlayer(player, this)
}