package powercards

import powercards.cards.{Copper, Estate}
import util.Random.shuffle

class Player(val name: String) extends Interactive {
  private val initDeck = shuffle(Seq.fill(3)(new Estate) ++ Seq.fill(7)(new Copper))
  var deck: Vector[Card] = initDeck.drop(5).toVector
  var hand: Vector[Card] = initDeck.take(5).toVector
  var played: Vector[Card] = Vector.empty
  var discard: Vector[Card] = Vector.empty

  var actions: Int = 0
  var buys: Int = 0
  var coins: Int = 0
}
