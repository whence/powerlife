import math

proc shuffle*[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

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

proc hasInOrder*(stack: openarray[string], hay: openarray[string]): bool = true
