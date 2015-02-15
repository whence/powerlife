import math
import algorithm

proc shuffle*[T](x: var seq[T]) =
  for i in countdown(x.high, 0):
    let j = random(i + 1)
    swap(x[i], x[j])

proc clear*[T](x: var seq[T]) = x.setlen(0)

proc replace*[T](x: var seq[T], y: openarray[T]) =
  x.clear
  x.add(y)

proc cycle*(x, cap: int): int =
  result = x + 1
  if result >= cap:
    result = 0

template any*(seq1, pred: expr): expr =
  var result {.gensym.}: bool = false
  for it {.inject.} in items(seq1):
    result = pred
    if result:
      break
  result

proc hasInOrder*[T](stack: openarray[T], hay: openarray[T]): bool =
  var
    istack = 0
    ihay = 0

  while istack < stack.len and ihay < hay.len:
    if stack[istack] == hay[ihay]:
      inc(ihay)
    inc(istack)

  return ihay > 0 and ihay == hay.len

when isMainModule:
  block: # sorting ints
    var ix = @[3, 1, 2, 4]
    ix.sort(system.cmp[int], SortOrder.Descending)
    assert ix == @[4, 3, 2, 1]

  block: # cycle
    assert cycle(0, 2) == 1
    assert cycle(1, 2) == 0

  block: # hasinorder
    block: # no gap
      let outs = ["wes", "bec"]
      assert outs.hasinorder(["wes", "bec"]) == true
      assert outs.hasinorder(["bec", "wes"]) == false
      assert outs.hasinorder(["wes", "Bec"]) == false
      assert outs.hasinorder([]) == false
    block: # has gap
      let outs = ["x", "wes", "y", "bec", "z"]
      assert outs.hasinorder(["wes", "bec"]) == true
      assert outs.hasinorder(["bec", "wes"]) == false
      assert outs.hasinorder(["wes", "bec", "y"]) == false
    block: # repeat
      let outs = ["wes", "bec", "x", "wes", "bec", "y"]
      assert outs.hasinorder(["wes", "bec"]) == true
      assert outs.hasinorder(["wes", "bec", "wes"]) == true
      assert outs.hasinorder(["wes", "bec", "bec"]) == true
      assert outs.hasinorder(["wes", "bec", "wes", "bec"]) == true
      assert outs.hasinorder(["wes", "bec", "y"]) == true
      assert outs.hasinorder(["x", "bec"]) == true
      assert outs.hasinorder(["wes", "bec", "bec", "wes"]) == false
      assert outs.hasinorder(["bec", "x", "x"]) == false
      
    