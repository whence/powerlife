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

type io = {
  input: unit -> string;
  output: string -> unit;
}
type requirement = Unlimited | MandatoryOne | OptionalOne
type choice = Unselectable | Skip | Indexes of int list

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

let choose io requirement message items =
  let out_item i item =
    sprintf "[%d] %s %s" i (fst item) (if snd item then "(select)" else "")
    |> io.output
  in
  let rec loop () =
    io.output message;
    List.iteri items ~f:out_item;
    if List.exists items ~f:snd then
      match requirement with
      | MandatoryOne ->
        let index = int_of_string (io.input()) in
        let (name, selectable) = List.nth_exn items index in
        if selectable then Indexes [index]
        else begin
          io.output (sprintf "%s is not selectable" name);
          loop()
        end
      | OptionalOne ->
        io.output "or skip";
        let raw_input = io.input() in
        if raw_input = "skip" then Skip
        else
          let index = int_of_string raw_input in
          let (name, selectable) = List.nth_exn items index in
          if selectable then Indexes [index]
          else begin
            io.output (sprintf "%s is not selectable" name);
            loop()
          end
      | Unlimited ->
        io.output "or all, or skip";
        match io.input() with
        | "skip" -> Skip
        | "all" ->
          let indexes = List.filter_mapi items ~f:(fun i (_, selectable) ->
              if selectable then Some i else None)
          in Indexes indexes
        | raw_input ->
          let indexes = raw_input
                        |> String.split ~on:','
                        |> List.map ~f:String.strip
                        |> List.filter ~f:(Fn.non String.is_empty)
                        |> List.map ~f:int_of_string
                        |> List.sort ~cmp:compare
          in
          if List.exists indexes ~f:(fun i ->
              let (_, selectable) = List.nth_exn items i in
              not selectable) then begin
            io.output "Some choices are not selectable";
            loop()
          end
          else
            Indexes indexes
    else Unselectable
  in
  loop()

let create_console_io () = {
  input = read_line;
  output = print_endline;
}

let create_recorded_io inputs =
  let inputs = ref inputs in
  let outputs = ref [] in
  let input () = match !inputs with
    | [] -> failwith "no more recorded input to use"
    | hd :: tl -> inputs := tl; hd
  in
  let output message = outputs := message :: !outputs in
  let dump_outputs () = List.rev !outputs in
  ({ input; output }, dump_outputs)

let rec draw_cards_loop n_remain acc =
  if n_remain = 0 then acc
  else match acc with
    | ([], _, [], _) -> acc
    | ([], hand, discard, drawed) ->
      draw_cards_loop n_remain (List.permute discard, hand, [], drawed)
    | (hd :: tl, hand, discard, drawed) ->
      draw_cards_loop (n_remain - 1) (tl, hd :: hand, discard, hd :: drawed)

let draw_cards n player =
  let (deck, hand, discard, drawed) = draw_cards_loop n (player.deck, player.hand, player.discard, []) in
  player.deck <- deck;
  player.hand <- hand;
  player.discard <- discard;
  drawed

let active_player game =
  List.nth_exn game.players game.active_player_index
