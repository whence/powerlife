package core

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

class CardLibrary {
  val estate = new BasicVictoryCard("Estate", 2, 1)
  val duchy = new BasicVictoryCard("Duchy", 5, 3)
  val province = new BasicVictoryCard("Province", 8, 6)

  val copper = new BasicTreasureCard("Copper", 0, 1)
  val silver = new BasicTreasureCard("Silver", 3, 2)
  val gold = new BasicTreasureCard("Gold", 6, 3)

  val remodel = new BasicActionCard("Remodel", 4, { game =>
    trashCard(game) flatMap {
  	  case (None, game) => Future.successful(game)
  	  case (Some(_), game) => gainCard(game).map(_._2)
  	}
  })
  
  val throneRoom = new BasicActionCard("Throne Room", 4, { game =>
  	import Selector._
    select(game.activePlayer.hand,
      CardCasts.toActionCard,
      MandatoryOne,
      game.activePlayer.name + ": select an action card to play twice"
    ) flatMap {
      case (selected, _) if selected.isEmpty =>
        println("no action card to play")
        Future.successful(game)
      case (selected, unselected) =>
        val card = selected.head
        println("playing " + card + " first time")
        card.play {
          game.updateActivePlayer { p => 
            p.copy(hand = unselected, played = p.played ++ selected)
          }
        } flatMap { game =>
          println("playing " + card + " second time")
          card.play(game)
        }
    }    
  })
  
  private[this] def trashCard(game: Game): Future[(Option[Card], Game)] = {
  	import Selector._
    select(game.activePlayer.hand,
      CardCasts.toCard,
      MandatoryOne,
      game.activePlayer.name + ": select a card to trash"
    ) map {
      case (selected, _) if selected.isEmpty =>
        println("nothing to trash")
        (None, game)
      case (selected, unselected) =>
        val card = selected.head
        println("trashed " + card)
        (Some(card), 
          game.updateActivePlayer { p =>
          p.copy(hand = unselected)
        } copy (
          trash = game.trash ++ selected
        ))
    }
  }
  
  private[this] val gainCard: Game => Future[(Option[Card], Game)] = { game =>
  	import Selector._
  	val supply: Vector[Card] = Vector(remodel, throneRoom)
    select(supply,
      CardCasts.toCard,
      MandatoryOne,
      game.activePlayer.name + ": select a card to gain"
    ) map {
      case (selected, _) if selected.isEmpty =>
        println("nothing to gain")
        (None, game)
      case (selected, unselected) =>
        val card = selected.head
        println("gained " + card)
        (Some(card), 
          game.updateActivePlayer { p =>
          p.copy(discard = p.discard ++ selected)
        })
    }
  }
}