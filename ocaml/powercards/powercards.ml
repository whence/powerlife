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
  | DynamicVictory of (player -> int)

let copper = { title = "Copper"; cost = 0; feature = BasicTreasure 1 }
let silver = { title = "Silver"; cost = 3; feature = BasicTreasure 2 }
let gold = { title = "Gold"; cost = 6; feature = BasicTreasure 3 }

let estate = { title = "Estate"; cost = 2; feature = BasicVictory 1 }
let duchy = { title = "Duchy"; cost = 5; feature = BasicVictory 3 }
let province = { title = "Province"; cost = 8; feature = BasicVictory 6 }

let garden =
  let vps p =
    let cnt = [p.deck; p.hand; p.played; p.discard]
              |> List.map ~f:List.length |> List.fold ~init:0 ~f:(+) in
    cnt / 10
  in { title ="Garden"; cost = 4; feature = DynamicVictory vps }

let remodel =
  let play _ _ = () in
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

let create_start_piles kingdom_cards player_cnt =
  let pile size sample = { sample; size } in
  let treasures = [pile (60 - player_cnt * 7) copper; pile 40 silver; pile 30 gold] in
  let victories =
    let n = match player_cnt with
      | 2 -> 8
      | 3 | 4 -> 12
      | _ -> assert false
    in [pile n estate; pile n duchy; pile n province]
  in
  let kingdoms = List.map kingdom_cards ~f:(pile 10) in
  treasures @ victories @ kingdoms

let create_game names =
  let player_cnt = List.length names in
  { players = List.map names ~f:create_player;
    active_player_index = Random.int player_cnt;
    piles = create_start_piles [remodel] player_cnt;
    trash = [];
    stage = Action;
    stat = { actions = 1; buys = 1; coins = 0 };
  }

let is_action card = match card.feature with
  | BasicAction _ -> true
  | SelfTrashAction _ -> true
  | BasicTreasure _ -> false
  | BasicVictory _ -> false
  | DynamicVictory _ -> false

let is_treasure card = match card.feature with
  | BasicAction _ -> false
  | SelfTrashAction _ -> false
  | BasicTreasure _ -> true
  | BasicVictory _ -> false
  | DynamicVictory _ -> false

let is_victory card = match card.feature with
  | BasicAction _ -> false
  | SelfTrashAction _ -> false
  | BasicTreasure _ -> false
  | BasicVictory _ -> true
  | DynamicVictory _ -> true

let same_kind x y = x.title = y.title

let find_pile game card = game.piles |> List.find_exn ~f:(fun p -> same_kind p.sample card)

let pile_empty pile = pile.size = 0

let split_by_indexes xs indexes =
  let aux i acc x = match acc with
    | (yes, no, hd :: tl) when i = hd -> (x :: yes, no, tl)
    | (yes, no, l) -> (yes, x :: no, l)
  in
  let (yes, no, _) = List.foldi xs ~f:aux ~init:([], [], indexes) in
  (List.rev yes, List.rev no)

let rec loop_til ~f = match f () with
  | Some x -> x
  | None -> loop_til ~f

let choose io requirement message items =
  let select_one input =
    let index = int_of_string input in
    match List.nth_exn items index with
    | (_, true) -> Some (Indexes [index])
    | (name, false) ->
      io.output (sprintf "%s is not selectable" name);
      None
  in
  let select_many input =
    let indexes = input
                  |> String.split ~on:','
                  |> List.map ~f:(String.strip ?drop:None)
                  |> List.filter ~f:(Fn.non String.is_empty)
                  |> List.map ~f:int_of_string
                  |> List.sort ~cmp:Int.compare
    in
    let nonSelectables = indexes
                         |> List.filter_map ~f:(fun i ->
                             let (title, selectable) = List.nth_exn items i in
                             if selectable then None else Some title)
    in
    if List.is_empty nonSelectables then
      Some (Indexes indexes)
    else begin
      nonSelectables
      |> String.concat ~sep:", "
      |> sprintf "%s are not selectable"
      |> io.output;
      None
    end
  in
  let ask () =
    io.output message;
    List.iteri items ~f:(fun i item ->
        sprintf "[%d] %s %s" i (fst item) (if snd item then "(select)" else "")
        |> io.output);
    match requirement with
    | MandatoryOne -> io.input () |> select_one
    | OptionalOne ->
      io.output "or skip";
      begin match io.input () with
      | "skip" -> Some Skip
      | input -> select_one input
      end
    | Unlimited ->
      io.output "or all, or skip";
      match io.input () with
      | "skip" -> Some Skip
      | "all" ->
        let indexes = List.filter_mapi items ~f:(fun i (_, selectable) ->
            if selectable then Some i else None)
        in Some (Indexes indexes)
      | input -> select_many input
  in
  if List.exists items ~f:snd then loop_til ~f:ask else Unselectable

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

let is_ended game =
  let cond1 () = find_pile game province |> pile_empty in
  let cond2 () = game.piles |> List.count ~f:pile_empty |> (<=) 3 in
  cond1 () || cond2 ()

let end_stat game =
  let card_vps p c = match c.feature with
    | BasicAction _ | SelfTrashAction _ | BasicTreasure _ -> 0
    | BasicVictory x -> x
    | DynamicVictory f -> f p
  in
  let player_vps p =
    [p.deck; p.hand; p.played; p.discard]
    |> List.concat
    |> List.fold ~init:0 ~f:(fun acc c -> acc + (card_vps p c))
  in
  let stats = game.players
              |> List.map ~f:(fun p -> (p.name, player_vps p))
              |> List.sort ~cmp:(fun (_, vps1) (_, vps2) -> Int.compare vps1 vps2)
              |> List.rev
  in
  let max_vps = List.hd_exn stats |> snd in
  stats |> List.map ~f:(fun (name, vps) -> (name, vps, (vps = max_vps)))

let play_one io game =
  let card_to_item predicate card = (card.title, predicate card) in
  let play_cards indexes =
    let player = active_player game in
    let (played, hand) = split_by_indexes player.hand indexes in
    player.hand <- hand;
    player.played <- played @ player.played;
    played
  in
  let buy_card index =
    let player = active_player game in
    let pile = List.nth_exn game.piles index in
    pile.size <- pile.size - 1;
    player.discard <- pile.sample :: player.discard;
    pile.sample
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
          | BasicTreasure _ | BasicVictory _ | DynamicVictory _ -> assert false
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
          | BasicAction _ | SelfTrashAction _ | BasicVictory _ | DynamicVictory _ ->
            assert false
        )
    end
  | Buy ->
    let skip_to_next () =
      game.stat.coins <- 0;
      game.stage <- Cleanup
    in
    if game.stat.buys > 0 then begin
      match game.piles
            |> List.map ~f:(fun p -> (p.sample.title, p.size > 0 && p.sample.cost <= game.stat.coins))
            |> choose io OptionalOne "Select a pile to buy"
      with
      | Unselectable ->
        io.output "No card to buys. Skip to cleanup stage";
        skip_to_next ()
      | Skip ->
        io.output "Skip to cleanup stage";
        skip_to_next ()
      | Indexes [index] ->
        game.stat.buys <- game.stat.buys - 1;
        let card = buy_card index in
        game.stat.coins <- game.stat.coins - card.cost;
        "Bought " ^ card.title |> io.output
      | Indexes _ -> assert false
    end else begin
      io.output "No more buys. Skip to cleanup stage";
      skip_to_next ()
    end
  | Cleanup ->
    let player = active_player game in
    player.discard <- player.played @ player.hand @ player.discard;
    player.played <- [];
    player.hand <- [];
    draw_cards 5 player |> ignore;
    game.active_player_index <- (if game.active_player_index = (List.length game.players) - 1
                                 then 0 else game.active_player_index + 1);
    game.stage <- Action;
    game.stat.actions <- 1;
    game.stat.buys <- 1;
    game.stat.coins <- 0

let play_til_end io game =
  let one () =
    play_one io game;
    if is_ended game then Some (end_stat game) else None
  in
  loop_til ~f:one
  |> List.iter ~f:(fun (name, vps, winner) ->
      sprintf "%s: %d %s" name vps (if winner then "(win)" else "") |> io.output)

let main () =
  let io = create_console_io () in
  let game = create_game ["wes";"bec"] in
  play_til_end io game
