package core

object Utils {
  val newline = sys.props("line.separator")

  def time(job: => Unit): Long = {
    val now = System.nanoTime()
    job
    val micros = (System.nanoTime() - now) / 1000
    micros
  }

  def insertionSort(a: Array[Int]) {
    var i = 1
    while (i < a.length) {
      // A[ i ] is added in the sorted sequence A[0, .. i-1]
      // save A[i] to make a hole at index iHole
      val item = a(i)
      var iHole = i
      // keep moving the hole to next smaller index until A[iHole - 1] is <= item
      while (iHole > 0 && a(iHole - 1) > item) {
        // move hole to next smaller index
        a(iHole) = a(iHole - 1)
        iHole = iHole - 1
      }
      // put item in the hole
      a(iHole) = item

      i += 1
    }
  }
}
