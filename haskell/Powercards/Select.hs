module Select (
    Requirement(..),
    select
) where

import qualified Utils as U

data Requirement = Unlimited | MandatoryOne | OptionalOne
    deriving (Show)

select :: Show a => [a] -> (a -> Bool) -> Requirement -> String -> IO [Int]
select [] _ _ _ = return []
select items predicate _ _ | not $ any predicate items = return []
select items predicate requirement message = 
    printDialog >> getLine >>= (handler requirement . filterInput . U.parseInput)
    where handler MandatoryOne matched@[_] = return matched
          handler MandatoryOne _ = putStrLn "you must select one item" >> again
          handler OptionalOne (_:_:_) = putStrLn "you cannot select more than one item" >> again
          handler OptionalOne matched = return matched
          handler Unlimited matched = return matched

          printDialog = putStrLn message >> print selected
          selected = filter (predicate . snd) $ zip [0..] items
          filterInput = filter filterer
          filterer index = index < length items && predicate (items !! index)
          again = select items predicate requirement message
