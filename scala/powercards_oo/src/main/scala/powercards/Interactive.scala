package powercards

trait Interactive {
  def io: IO
  def chooseOne(message: String, items: IndexedSeq[Item]): choices.one.Choice = ???
  def chooseOptionalOne(message: String, items: IndexedSeq[Item]): choices.optional_one.Choice = ???
}
