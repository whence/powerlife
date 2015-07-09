package powercards

trait Stage {
  def play(game: Game): Stage
}
