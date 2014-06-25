package core

object Keys {
  private[this] val maxPlayerCount = 4

  // zone keys
  val deck = 0
  val hand = deck + maxPlayerCount
  val played = hand + maxPlayerCount
  val discard = played + maxPlayerCount

  val trash = discard + 1

  // stat keys
  val playerCount = 0
  val activePlayerIndex = playerCount + 1
  val stage = activePlayerIndex + 1

  val actions = stage + maxPlayerCount
  val buys = actions + maxPlayerCount
  val coins = buys + maxPlayerCount

  // stage keys
  val action = 0
  val treasure = action + 1
  val buy = treasure + 1
  val cleanup = buy + 1
}
