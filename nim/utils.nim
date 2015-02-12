import math
import sequtils
import algorithm

proc shuffle*[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

proc randomPairs*(m: int): seq[tuple[a: int, b: int]] =
  newseq(result, m)
  for a in 0 .. m-1:
    let i = m - 1 - a
    let j = random(i + 1)
    result[a] = (i, j)

proc clear*[T](x: var seq[T]) = x.setlen(0)

proc replace*[T](x: var seq[T], y: openarray[T]) =
  x.clear
  x.add(y)

proc cycle*(x, cap: int): int =
  let y = x + 1
  return if y >= cap: 0 else: y

template any*(seq1, pred: expr): expr =
  var result {.gensym.}: bool = false
  for it {.inject.} in items(seq1):
    result = pred
    if result:
      break
  result

proc sequals[T](x: seq[T], y: openarray[T]): bool =
  ## structure equals
  if x.len != y.len:
    return false
  for i in 0 .. <x.len:
    if x[i] != y[i]:
      return false
  return true    

proc hasInOrder*[T](stack: seq[T], hay: openarray[T]): bool =
  filterit(stack, hay.contains(it)).sequals(hay)

when isMainModule:
  block: # sequals
    assert(@[1,2,3].sequals([1,2,3]))
    assert(not @[1,3,2].sequals([1,2,3]))
    assert(not @[1,3,2].sequals([1,2,3,4]))

  block: # hasinorder
    assert(@["wes", "bec"].hasinorder(["wes", "bec"]))
    assert(not @["wes", "bec"].hasinorder(["bec", "wes"]))
    assert(not @["wes", "bec"].hasinorder(["wes", "Bec"]))

    assert(@["x", "wes", "y", "bec", "z"].hasinorder(["wes", "bec"]))
    assert(not @["wes", "x", "bec"].hasinorder(["bec", "wes"]))
    assert(not @["y", "wes", "bec"].hasinorder(["wes", "bec", "y"]))

    assert(@["wes", "bec", "x", "wes", "bec", "y"].hasinorder(["wes", "bec", "wes", "bec"]))

    assert(@[1, 2, 3].hasinorder([2, 3]))
    assert(not @[1, 2, 3].hasinorder([2, 2]))