module Utils where

import qualified Data.List as List

parseInput :: String -> [Int]
parseInput = List.nub . read

divide :: [a] -> [Int] -> ([a], [a])
divide items indexes = (selected, unselected)
    where selected = map (items!!) inIndexes
          unselected = map (items!!) notIndexes
          inIndexes = filter (`elem` allIndexes items) cappedIndexes
          notIndexes = filter (not . (`elem` inIndexes)) (allIndexes items)
          allIndexes [] = []
          allIndexes _ = [0..(pred $ length items)]
          cappedIndexes = take (length items) indexes

matchAll :: [(a -> Bool)] -> a -> Bool
matchAll ps x = all ($x) ps

matchAny :: [(a -> Bool)] -> a -> Bool
matchAny ps x = any ($x) ps
