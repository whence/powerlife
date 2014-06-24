package core

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

object Game {
  def apply(playerNames: Vector[String]): Game = {
    implicit val cardlib = new CardLibrary
    new Game(
      playerNames.map(Player.apply),
      util.Random.nextInt(playerNames.length),
      ActionStage(1, 1, 0),
      Vector.empty[Card])
  }
}

case class Game(players: Vector[Player], activePlayerIndex: Int, stage: TurnStage, trash: Vector[Card]) {
  def play: Future[Game] = playOne flatMap { _.play }

  def playOne: Future[Game] = {
    stage match {
      case ActionStage(0, buys, coins) =>
        Future.successful(copy(stage = TreasureStage(buys, coins)))
      case ActionStage(_, buys, coins) =>
        import Selector._
        select(activePlayer.hand,
          CardCasts.toActionCard,
          OptionalOne,
          activePlayer.name + ": select an action card to play"
        ) flatMap {
          case (selected, _) if selected.isEmpty =>
            Future.successful(copy(stage = TreasureStage(buys, coins)))
          case (selected, unselected) =>
            selected.head.play {
              updateActivePlayer { p => 
                p.copy(hand = unselected, played = p.played ++ selected)
              }
            } map { 
              case game@(Game(_, _, ActionStage(actions, buys, coins), _))=> 
              	game.copy(stage = ActionStage(actions - 1, buys, coins))
            }
        }
      case TreasureStage(buys, coins) =>
        import Selector._
        select(activePlayer.hand,
          CardCasts.toTreasureCard,
          Unlimited,
          activePlayer.name + ": select treasure cards to play"
        ) flatMap {
          case (selected, _) if selected.isEmpty =>
            Future.successful(copy(stage = BuyStage(buys, coins)))
          case (selected, unselected) =>
            Future.successful {
              updateActivePlayer { p => 
	            p.copy(hand = unselected, played = p.played ++ selected)
              } copy (
	            stage = {
	              val coins = selected.map(x => x.coin).sum
	              println("gaining " + coins + " coins")
	              TreasureStage(buys, coins + coins)
	            }
              )
        	}
        }
      case BuyStage(0, _) =>
        Future.successful(copy(stage = CleanupStage))
      case BuyStage(_, _) =>
        Future.successful(copy(stage = CleanupStage))
      case CleanupStage =>
        Future.successful(copy(
          players = players.updated(activePlayerIndex, activePlayer.cleanup),
          activePlayerIndex = (if (activePlayerIndex == players.length - 1) 0 else activePlayerIndex + 1),
          stage = ActionStage(1, 1, 0)
        ))
    }
  }

  def activePlayer: Player = players(activePlayerIndex)
  
  def updatePlayer(playerIndex: Int, f: Player => Player): Game = 
    copy(players = players.updated(playerIndex, f(players(playerIndex))))
  
  def updateActivePlayer(f: Player => Player): Game =
    updatePlayer(activePlayerIndex, f)
}

trait TurnStage

case class ActionStage(actions: Int, buys: Int, coins: Int) extends TurnStage
case class TreasureStage(buys: Int, coins: Int) extends TurnStage
case class BuyStage(buys: Int, coins: Int) extends TurnStage
case object CleanupStage extends TurnStage