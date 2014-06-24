package powercards

object Accessors {
  import Core._
  
  def get[A](acc: Accessor[A])(game: Game): A =
    acc.unpack(game(acc.keygen(game)))
    
  def set[A](acc: Accessor[A], value: A)(game: Game): Game =
    game.updated(acc.keygen(game), acc.pack(value))
    
  def update[A](acc: Accessor[A], f: A => A)(game: Game): Game = {
    val key = acc.keygen(game)
    game.updated(key, acc.pack(f(acc.unpack(game(key)))))
  }
  
  trait Accessor[A] {
    def keygen(game: Game): Int
    def pack(a: A): Box
    def unpack(box: Box): A
  }
  
  private[Accessors] trait CardAccessor extends Accessor[Vector[Card]] {
    def pack(cards: Vector[Card]): Box = CardBox(cards)
    def unpack(box: Box): Vector[Card] = box match { case CardBox(cards) => cards }
  }
  
  private[Accessors] trait IntAccessor extends Accessor[Int] {
    def pack(n: Int): Box = IntBox(n)
    def unpack(box: Box): Int = box match { case IntBox(n) => n }
  }
  
  private[Accessors] trait StringAccessor extends Accessor[String] {
    def pack(str: String): Box = StringBox(str)
    def unpack(box: Box): String = box match { case StringBox(str) => str }
  }
  
  private[Accessors] trait StageAccessor extends Accessor[Stage] {
    def pack(stage: Stage): Box = StageBox(stage)
    def unpack(box: Box): Stage = box match { case StageBox(stage) => stage }
  }
  
  private[Accessors] abstract class ConstKeyGen(key: Int) {
    def keygen(game: Game): Int = key
  }
  
  private[Accessors] class ConstKeyCardAccessor(key: Int) extends ConstKeyGen(key) with CardAccessor
  private[Accessors] class ConstKeyIntAccessor(key: Int) extends ConstKeyGen(key) with IntAccessor

  private[Accessors] object BaseKeys {
    val maxPlayerCount: Int = 4
    val mpc = maxPlayerCount
    
    val deck = mpc * 0
    val hand = mpc * 1
    val played = mpc * 2
    val discard = mpc * 3
    val name = mpc * 4
    
    val trash = mpc * 5 + 0
    val stage = mpc * 5 + 1
    val activePlayerIndex = mpc * 5 + 2
    val playerCount = mpc * 5 + 3
    val actions = mpc * 5 + 4
    val buys = mpc * 5 + 5
    val coins = mpc * 5 + 6
  }
  
  trait PlayerAccessors {
    def deck: Accessor[Vector[Card]]
    def hand: Accessor[Vector[Card]]
    def played: Accessor[Vector[Card]]
    def discard: Accessor[Vector[Card]]
    def name: Accessor[String]
  }
  
  object player {
    def apply(index: Int): PlayerAccessors = new PlayerAccessors {
      val deck = new ConstKeyCardAccessor(BaseKeys.deck + index)
      val hand = new ConstKeyCardAccessor(BaseKeys.hand + index)
      val played = new ConstKeyCardAccessor(BaseKeys.played + index)
      val discard = new ConstKeyCardAccessor(BaseKeys.discard + index)
      val name = new ConstKeyGen(BaseKeys.name + index) with StringAccessor
    }
    
    val active: PlayerAccessors = new PlayerAccessors {
      val deck = new CardAccessor { def keygen(game: Game): Int = BaseKeys.deck + api(game) }
      val hand = new CardAccessor { def keygen(game: Game): Int = BaseKeys.hand + api(game) }
      val played = new CardAccessor { def keygen(game: Game): Int = BaseKeys.played + api(game) }
      val discard = new CardAccessor { def keygen(game: Game): Int = BaseKeys.discard + api(game) }
      val name = new StringAccessor { def keygen(game: Game): Int = BaseKeys.name + api(game) }
      
      private def api(game: Game): Int = get(globals.activePlayerIndex)(game)
    }
  }
  
  object globals {
    val trash: Accessor[Vector[Card]] = new ConstKeyCardAccessor(BaseKeys.trash)
    val stage: Accessor[Stage] = new ConstKeyGen(BaseKeys.stage) with StageAccessor
    val activePlayerIndex: Accessor[Int] = new ConstKeyIntAccessor(BaseKeys.activePlayerIndex)
    val playerCount: Accessor[Int] = new ConstKeyIntAccessor(BaseKeys.playerCount)
    val actions: Accessor[Int] = new ConstKeyIntAccessor(BaseKeys.actions)
    val buys: Accessor[Int] = new ConstKeyIntAccessor(BaseKeys.buys)
    val coins: Accessor[Int] = new ConstKeyIntAccessor(BaseKeys.coins)
  }
}