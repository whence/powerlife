open Core.Std

type stage = Action | Treasure | Buy | Cleanup

type io = {
  input: unit -> string;
  output: string -> unit;
}
type requirement = Unlimited | MandatoryOne | OptionalOne
type choice = Unselectable | Skip | Indexes of int list

type stat = {
  mutable actions: int;
  mutable buys: int;
  mutable coins: int;
}

type game = {
  players: player list;
  mutable active_player_index: int;
  piles: pile list;
  mutable trash: card list;
  mutable stage: stage;
  stat: stat;
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
and card = {
  title: string;
  cost: int;
  feature: card_feature;
}
and card_feature =
  | BasicAction of (io -> game -> unit)
  | SelfTrashAction of (io -> game * bool -> bool)
  | BasicTreasure of int
  | BasicVictory of int

let copper = { title = "Copper"; cost = 0; feature = BasicTreasure 1 }
let silver = { title = "Silver"; cost = 3; feature = BasicTreasure 2 }
let gold = { title = "Gold"; cost = 6; feature = BasicTreasure 3 }

let estate = { title = "Estate"; cost = 2; feature = BasicVictory 1 }
let duchy = { title = "Duchy"; cost = 5; feature = BasicVictory 3 }
let province = { title = "Province"; cost = 8; feature = BasicVictory 6 }

let remodel =
  let play io game = () in
  { title = "Remodel"; cost = 4; feature = BasicAction play }

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
    stage = Action;
    stat = { actions = 1; buys = 1; coins = 0 };
  }

let is_action card = match card.feature with
  | BasicAction _ -> true
  | SelfTrashAction _ -> true
  | BasicTreasure _ -> false
  | BasicVictory _ -> false

let is_treasure card = match card.feature with
  | BasicAction _ -> false
  | SelfTrashAction _ -> false
  | BasicTreasure _ -> true
  | BasicVictory _ -> false

let is_victory card = match card.feature with
  | BasicAction _ -> false
  | SelfTrashAction _ -> false
  | BasicTreasure _ -> false
  | BasicVictory _ -> true

let same_kind x y = x.title = y.title

let split_by_indexes xs indexes =
  let aux i acc x = match acc with
    | (yes, no, hd :: tl) when i = hd -> (x :: yes, no, tl)
    | (yes, no, l) -> (yes, x :: no, l)
  in
  let (yes, no, _) = List.foldi xs ~f:aux ~init:([], [], indexes) in
  (List.rev yes, List.rev no)

let choose io requirement message items =
  let rec loop () =
    io.output message;
    List.iteri items ~f:(fun i item ->
        sprintf "[%d] %s %s" i (fst item) (if snd item then "(select)" else "")
        |> io.output);
    match requirement with
    | MandatoryOne ->
      let index = io.input () |> int_of_string in
      let (name, selectable) = List.nth_exn items index in
      if selectable then Indexes [index]
      else begin
        io.output (sprintf "%s is not selectable" name);
        loop ()
      end
    | OptionalOne ->
      io.output "or skip";
      let raw_input = io.input () in
      if raw_input = "skip" then Skip
      else
        let index = int_of_string raw_input in
        let (name, selectable) = List.nth_exn items index in
        if selectable then Indexes [index]
        else begin
          io.output (sprintf "%s is not selectable" name);
          loop ()
        end
    | Unlimited ->
      io.output "or all, or skip";
      match io.input () with
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
                      |> List.sort ~cmp:Int.compare
        in
        if List.exists indexes ~f:(fun i ->
            let (_, selectable) = List.nth_exn items i in
            not selectable) then begin
          io.output "Some choices are not selectable";
          loop ()
        end
        else
          Indexes indexes
  in
  if List.exists items ~f:snd then loop () else Unselectable

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

let play_one io game =
  let card_to_item predicate card = (card.title, predicate card) in
  let play_cards indexes =
    let player = active_player game in
    let (played, hand) = split_by_indexes player.hand indexes in
    player.hand <- hand;
    player.played <- played @ player.played;
    played
  in
  match game.stage with
  | Action ->
    let skip_to_next () =
      game.stat.actions <- 0;
      game.stage <- Treasure
    in
    if game.stat.actions > 0 then begin
      match (active_player game).hand
            |> List.map ~f:(card_to_item is_action)
            |> choose io OptionalOne "Select an action card to play"
      with
      | Unselectable ->
        io.output "No action card to play. Skip to treasure stage";
        skip_to_next ()
      | Skip ->
        io.output "Skip to treasure stage";
        skip_to_next ()
      | Indexes indexes ->
        game.stat.actions <- game.stat.actions - 1;
        match play_cards indexes with
        | [card] ->
          "playing " ^ card.title |> io.output;
          begin match card.feature with
          | BasicAction play -> play io game
          | SelfTrashAction play -> play io (game, false) |> ignore
          | BasicTreasure _ | BasicVictory _ -> assert false
          end
        | _ -> assert false
    end else begin
      io.output "No action point. Skip to treasure stage";
      skip_to_next ()
    end
  | Treasure ->
    let skip_to_next () =
      game.stat.buys <- 0;
      game.stage <- Buy
    in
    begin match (active_player game).hand
          |> List.map ~f:(card_to_item is_treasure)
          |> choose io Unlimited "Select treasure cards to play"
    with
    | Unselectable ->
      io.output "No treasure card to play. Skip to buy stage";
      skip_to_next ()
    | Skip ->
      io.output "Skip to buy stage";
      skip_to_next ()
    | Indexes indexes ->
      let cards = play_cards indexes in
      cards
      |> List.map ~f:(fun c -> c.title)
      |> String.concat ~sep:","
      |> (^) "playing "
      |> io.output;
      List.iter cards ~f:(fun c -> match c.feature with
          | BasicTreasure coins -> game.stat.coins <- game.stat.coins + coins
          | BasicAction _ | SelfTrashAction _ | BasicVictory _ -> assert false
        )
    end
  | _ -> assert false
