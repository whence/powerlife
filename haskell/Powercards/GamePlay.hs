module GamePlay where

import Core
import qualified Utils as U
import qualified Select as S
import Control.Monad (foldM_)
import Data.Maybe (fromJust, isJust)
import Data.Graph.Inductive.Query.Monad (mapSnd)

createGame :: [String] -> Game
createGame [] = error "must have at least 1 player"
createGame playerNames = addGlobals . addPlayers $ emptyGame
    where addPlayers :: Game -> Game
          addPlayers game = foldr addPlayer game $ zip [0..] playerNames

          addPlayer :: (Int, String) -> Game -> Game
          addPlayer (n, playerName) = 
            set (pDeck n) (drop 5 startDeck) .
            set (pHand n) (take 5 startDeck ++ [remodelCard, throneRoomCard]) .
            set (pPlayed n) [] .
            set (pDiscard n) [] .
            set (pName n) playerName

          addGlobals :: Game -> Game
          addGlobals = 
            set gTrash [] .
            set gStage Action .
            set gActivePlayerIndex 0 .
            set gPlayerCount (length playerNames) .
            set gActions 1 .
            set gBuys 1 .
            set gCoins 0

          startDeck :: [Card]
          startDeck = replicate 3 estateCard ++ replicate 7 copperCard

play :: Game -> IO ()
play game = foldM_ playOne game [1..]

playOne :: Game -> Int -> IO Game
playOne game _ | get gStage game == Action && get gActions game == 0 =
    return . set gStage Treasure $ game
playOne game _ | get gStage game == Action && get gActions game > 0 =
    S.select 
        (get aHand game) 
        (isJust . toActionCard)
        S.OptionalOne 
        (get aName game ++ ": select an action card to play")
    >>= handler
    where handler [] = do
            putStrLn "skip to treasure stage"
            return . set gStage Treasure . set gActions 0 $ game
          handler indexes@[_] =
            playCard . mapSnd (update gActions pred) . moveCards aHand aPlayed indexes $ game
          playCard ([card], game) = 
            putStrLn ("playing " ++ show card) >>
            (fromJust . toActionCard) card game
playOne game _ | get gStage game == Treasure =
    S.select 
        (get aHand game) 
        (isJust . toTreasureCard)
        S.Unlimited
        (get aName game ++ ": select treasure cards to play")
    >>= handler
    where handler [] = do
            putStrLn "skip to buy stage"
            return . set gStage Buy $ game
          handler indexes =
            gainCoin . moveCards aHand aPlayed indexes $ game
          gainCoin (cards, game) = do
            let coins = sum $ map (fromJust . toTreasureCard) cards
                game' = update gCoins (+coins) game
            putStrLn ("gained " ++ show coins ++ " coins")
            return game'
playOne game _ | get gStage game == Buy && get gBuys game == 0 =
    return . set gStage Cleanup $ game
playOne game _ | get gStage game == Buy && get gBuys game > 0 = do
    putStrLn "skipping buy stage as not implemented"
    return . set gStage Cleanup $ game
playOne game _ | get gStage game == Cleanup =
    return . reset . cleanup $ game
    where cleanup = drawCards aDeck aHand aDiscard 5 .
                    snd . moveCards aDiscard aPlayed [0..] . 
                    snd . moveCards aHand aPlayed [0..]
          reset = update gActivePlayerIndex nextApi . 
                  set gActions 1 . set gBuys 1 . set gCoins 0 . 
                  set gStage Action
          nextApi api = cycle [0..(get gPlayerCount game - 1)] !! (api + 1)

moveCards :: BoxAccess [Card] -> BoxAccess [Card] -> [Int] -> Game -> ([Card], Game)
moveCards ba1 ba2 indexes game =
    let v1 = get ba1 game
        (selected, unselected) = U.divide v1 indexes
        withdraw = set ba1 unselected
        deposit = update ba2 (++selected)
        pack g = (selected, g)
    in  pack . deposit . withdraw $ game

drawCards:: BoxAccess [Card] -> BoxAccess [Card] -> BoxAccess [Card] -> Int -> Game -> Game
drawCards _ _ _ 0 game = game
drawCards src dst bak n game =
    let (cards', game') = moveCards src dst [0..(n-1)] game
    in  case n - length cards' of 
        0 -> game'
        n' | n' > 0 -> snd . moveCards src dst [0..(n'-1)] . 
                       snd . moveCards bak src [0..] $ game'

trashCard :: Game -> IO (Maybe Card, Game)
trashCard game =
    S.select
        (get aHand game) 
        (const True)
        S.MandatoryOne
        "select a card to trash"
    >>= handler
    where handler [] = 
            putStrLn "No card to trash" >>
            return (Nothing, game)
          handler indexes@[_] =
            let ([card], game') = moveCards aHand gTrash indexes game
            in  putStrLn ("trashed " ++ show card) >> return (Just card, game')

gainCard :: Game -> IO (Maybe Card, Game)
gainCard game =
    S.select 
        supply
        (const True)
        S.MandatoryOne
        "select a card to gain"
    >>= handler
    where handler [] =
            putStrLn "No card to gain" >>
            return (Nothing, game)
          handler indexes@[_] =
            let (selected@[card], _) = U.divide supply indexes
                game' = update aDiscard (++selected) game
            in  putStrLn ("gained " ++ show card) >> return (Just card, game') 
          supply = [remodelCard, throneRoomCard]

estateCard :: Card
estateCard = BasicVictoryCard "Estate" 2 1

duchyCard :: Card
duchyCard = BasicVictoryCard "Duchy" 5 3

provinceCard :: Card
provinceCard = BasicVictoryCard "Province" 8 6

copperCard :: Card
copperCard = BasicTreasureCard "Copper" 0 1

silverCard :: Card
silverCard = BasicTreasureCard "Silver" 3 2

goldCard :: Card
goldCard = BasicTreasureCard "Gold" 6 3

remodelCard :: Card
remodelCard = BasicActionCard "Remodel" 4 cardPlay
    where cardPlay = (>>= handler) . trashCard
          handler (Just _, game) = fmap snd $ gainCard game
          handler (Nothing, game) = return game

throneRoomCard :: Card
throneRoomCard = BasicActionCard "Throne Room" 4 cardPlay
    where cardPlay game =
            S.select 
                (get aHand game) 
                (isJust . toActionCard)
                S.MandatoryOne 
                "select an action card to play twice"
            >>= handler
            where handler [] =
                    putStrLn "No action card to play" >>
                    return game
                  handler indexes@[_] =
                    playCardTwice . moveCards aHand aPlayed indexes $ game
                  playCardTwice ([card], game) = do
                    putStrLn ("playing " ++ show card ++ " first time")
                    game' <- (fromJust . toActionCard) card game
                    putStrLn ("playing " ++ show card ++ " second time")
                    (fromJust . toActionCard) card game'
