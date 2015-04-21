open Core.Std

type stage =
  | Action of int * int * int
  | Treasure of int * int
  | Buy of int * int
  | Cleanup

type game = {
    players: player list;
    mutable active_player_index: int;
    piles: pile list;
    mutable trash: card list;
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
  }
and card =
  | BasicActionCard of string * int * (game -> unit)
  | SelfTrashActionCard of string * int * (game * bool -> bool)
  | BasicTreasureCard of string * int * int
  | BasicVictoryCard of string * int * int

let copper = BasicTreasureCard ("Copper", 0, 1)
let silver = BasicTreasureCard ("Silver", 3, 2)
let gold = BasicTreasureCard ("Gold", 6, 3)

let estate = BasicVictoryCard ("Estate", 2, 1)
let duchy = BasicVictoryCard ("Duchy", 5, 3)
let province = BasicVictoryCard ("Province", 8, 6)

let remodel =
  let play game = () in
  BasicActionCard ("Remodel", 4, play)

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

let create_start_piles kingdom_cards =
  let pile size sample = { sample; size } in
  let commons = [pile 60 copper; pile 12 estate] in
  let kingdoms = List.map kingdom_cards ~f:(pile 10) in
  commons @ kingdoms

let create_game names =
  { players = List.map names ~f:create_player;
    active_player_index = Random.int (List.length names);
    piles = create_start_piles [remodel];
    trash = [];
    stage = Action (1, 1, 0);
  }

let card_name = function
  | BasicActionCard (name, _, _) -> name
  | SelfTrashActionCard (name, _, _) -> name
  | BasicTreasureCard (name, _, _) -> name
  | BasicVictoryCard (name, _, _) -> name

let card_cost = function
  | BasicActionCard (_, cost, _) -> cost
  | SelfTrashActionCard (_, cost, _) -> cost
  | BasicTreasureCard (_, cost, _) -> cost
  | BasicVictoryCard (_, cost, _) -> cost

let is_action = function
  | BasicActionCard (_, _, _) -> true
  | SelfTrashActionCard (_, _, _) -> true
  | BasicTreasureCard (_, _, _) -> false
  | BasicVictoryCard (_, _, _) -> false

let is_treasure = function
  | BasicActionCard (_, _, _) -> false
  | SelfTrashActionCard (_, _, _) -> false
  | BasicTreasureCard (_, _, _) -> true
  | BasicVictoryCard (_, _, _) -> false

let is_victory = function
  | BasicActionCard (_, _, _) -> false
  | SelfTrashActionCard (_, _, _) -> false
  | BasicTreasureCard (_, _, _) -> false
  | BasicVictoryCard (_, _, _) -> true

let same_kind x y = (card_name x) = (card_name y)

let split_by_indexes xs indexes =
  let aux i acc x = match acc with
    | (yes, no, hd :: tl) when i = hd -> (x :: yes, no, tl)
    | (yes, no, l) -> (yes, x :: no, l)
  in
  let (yes, no, _) = List.foldi xs ~f:aux ~init:([], [], indexes) in
  (List.rev yes, List.rev no)
                                           
