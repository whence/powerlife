type stage =
  | Action of int * int * int
  | Treasure of int * int
  | Buy of int * int
  | Cleanup

type game = {
    players: player list;
    piles: pile list;
    mutable trash: card list;
    mutable active_player_index: int;
    mutable stage: stage;
  }
and player = {
    name: string;
    mutable deck: card list;
    mutable hand: card list;
    mutable played: card list;
    mutable discard: card list;
  }
and pile = {
    sample: card;
    mutable size: int;
    generator: unit -> card;
    mutable buffer: card list;
  }
and card =
  | BasicActionCard of string * int * (game -> unit)
  | SelfTrashActionCard of string * int * (game * bool -> bool)
  | BasicTreasureCard of string * int * int
  | BasicVictoryCard of string * int * int
  | ComboCard of card * card
