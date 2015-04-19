open Core.Std

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
    mutable buffer: card list;
  }
and card =
  | BasicActionCard of string * int * (game -> unit)
  | SelfTrashActionCard of string * int * (game * bool -> bool)
  | BasicTreasureCard of string * int * int
  | BasicVictoryCard of string * int * int
  | ComboCard of card * card

let copper = BasicTreasureCard ("Copper", 0, 1)
let silver = BasicTreasureCard ("Silver", 3, 2)
let gold = BasicTreasureCard ("Gold", 6, 3)

let estate = BasicVictoryCard ("Estate", 2, 1)
let duchy = BasicVictoryCard ("Duchy", 5, 3)
let province = BasicVictoryCard ("Province", 8, 6)

let create_player name =
  let deck = 
    let estates = List.init 3 ~f:(fun _ -> estate) in
    let coppers = List.init 7 ~f:(fun _ -> copper) in
    List.permute (estates @ coppers)
  in
  { name;
    deck = List.take deck 5;
    hand = List.drop deck 5;
    played = [];
    discard = [];
  }
