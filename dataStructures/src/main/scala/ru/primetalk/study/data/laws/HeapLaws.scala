package ru.primetalk.study.data.laws

import org.scalacheck.{Platform, Properties}
import ru.primetalk.study.data.Heap
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object HeapLaws:

  class HeapProperties[H[*]: Heap](name: String) extends Properties(name):
    val h = summon[Heap[H]]
    property("insertToEmptyIsSameAsUnit") = forAll{ (i: Int) =>
      h.insert(h.empty[Int], 0) == h.unit(0)
    }
    property("toListDesc") = forAll{ (lst: List[Int]) =>
      h.toListDesc(Heap.fromList(lst)) == lst.sorted.reverse
    }
    property("findMin") = forAll{ (lst: List[Int]) =>
      h.findMin(Heap.fromList(lst)) == lst.minOption
    }
