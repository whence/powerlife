module Core where

import qualified Utils as U
import qualified Select as S
import Data.HashMap.Lazy (HashMap, (!), insert, adjust, empty)

type Game = HashMap Int Box

emptyGame :: Game
emptyGame = empty

data Box
    = CardBox [Card]
    | IntBox Int
    | StageBox Stage
    | StringBox String

instance Show Box where
    show (CardBox cards) = show cards
    show (IntBox n) = show n
    show (StageBox stage) = show stage
    show (StringBox str) = str

data Stage = Action | Treasure | Buy | Cleanup 
    deriving (Eq, Show)

data Card
    = BasicActionCard String Int (Game -> IO Game) -- name, costs, play
    | BasicTreasureCard String Int Int -- name, costs, coins
    | BasicVictoryCard String Int Int -- name, costs, vps

instance Show Card where
    show (BasicActionCard name _ _) = name
    show (BasicTreasureCard name _ _) = name
    show (BasicVictoryCard name _ _) = name

instance Eq Card where
    (==) (BasicActionCard name1 _ _) (BasicActionCard name2 _ _) = name1 == name2
    (==) (BasicTreasureCard name1 _ _) (BasicTreasureCard name2 _ _) = name1 == name2
    (==) (BasicVictoryCard name1 _ _) (BasicVictoryCard name2 _ _) = name1 == name2
    (==) _ _ = False

toActionCard :: Card -> Maybe (Game -> IO Game)
toActionCard (BasicActionCard _ _ cardPlay) = Just cardPlay
toActionCard _ = Nothing

toTreasureCard :: Card -> Maybe Int
toTreasureCard (BasicTreasureCard _ _ coins) = Just coins
toTreasureCard _ = Nothing

toVictoryCard :: Card -> Maybe Int
toVictoryCard (BasicVictoryCard _ _ vps) = Just vps
toVictoryCard _ = Nothing

newtype BoxAccess a = BoxAccess ((Game -> Int), (a -> Box), (Box -> a))

get :: BoxAccess a -> Game -> a
get (BoxAccess (keygen, _, unpack)) game = unpack $ game ! (keygen game)

set :: BoxAccess a -> a -> Game -> Game
set (BoxAccess (keygen, pack, _)) val game = insert (keygen game) (pack val) game

update :: BoxAccess a -> (a -> a) -> Game -> Game
update (BoxAccess (keygen, pack, unpack)) f game = adjust (pack . f . unpack) (keygen game) game

_maxPlayerNum :: Int
_maxPlayerNum = 4

_aKeygen :: Int -> Game -> Int
_aKeygen baseNum game = _maxPlayerNum * baseNum + get gActivePlayerIndex game

_pKeygen :: (Int, Int) -> Game -> Int
_pKeygen (baseNum, playerIndex) game = _maxPlayerNum * baseNum + playerIndex

_unpackCards :: Box -> [Card]
_unpackCards (CardBox cards) = cards

_unpackInt :: Box -> Int
_unpackInt (IntBox n) = n

_unpackStage :: Box -> Stage
_unpackStage (StageBox stage) = stage

_unpackString :: Box -> String
_unpackString (StringBox str) = str

aDeck :: BoxAccess [Card]
aDeck = BoxAccess (_aKeygen 0, CardBox, _unpackCards)

pDeck :: Int -> BoxAccess [Card]
pDeck n = BoxAccess (_pKeygen (0, n), CardBox, _unpackCards)

aHand :: BoxAccess [Card]
aHand = BoxAccess (_aKeygen 1, CardBox, _unpackCards)

pHand :: Int -> BoxAccess [Card]
pHand n = BoxAccess (_pKeygen (1, n), CardBox, _unpackCards)

aPlayed :: BoxAccess [Card]
aPlayed = BoxAccess (_aKeygen 2, CardBox, _unpackCards)

pPlayed :: Int -> BoxAccess [Card]
pPlayed n = BoxAccess (_pKeygen (2, n), CardBox, _unpackCards)

aDiscard :: BoxAccess [Card]
aDiscard = BoxAccess (_aKeygen 3, CardBox, _unpackCards)

pDiscard :: Int -> BoxAccess [Card]
pDiscard n = BoxAccess (_pKeygen (3, n), CardBox, _unpackCards)

aName :: BoxAccess String
aName = BoxAccess (_aKeygen 4, StringBox, _unpackString)

pName :: Int -> BoxAccess String
pName n = BoxAccess (_pKeygen (4, n), StringBox, _unpackString)

gTrash :: BoxAccess [Card]
gTrash = BoxAccess (const (_maxPlayerNum * 5 + 0), CardBox, _unpackCards)

gStage :: BoxAccess Stage
gStage = BoxAccess (const (_maxPlayerNum * 5 + 1), StageBox, _unpackStage)

gActivePlayerIndex :: BoxAccess Int
gActivePlayerIndex = BoxAccess (const (_maxPlayerNum * 5 + 2), IntBox, _unpackInt)

gPlayerCount :: BoxAccess Int
gPlayerCount = BoxAccess (const (_maxPlayerNum * 5 + 3), IntBox, _unpackInt)

gActions :: BoxAccess Int
gActions = BoxAccess (const (_maxPlayerNum * 5 + 4), IntBox, _unpackInt)

gBuys :: BoxAccess Int
gBuys = BoxAccess (const (_maxPlayerNum * 5 + 5), IntBox, _unpackInt)

gCoins :: BoxAccess Int
gCoins = BoxAccess (const (_maxPlayerNum * 5 + 6), IntBox, _unpackInt)
