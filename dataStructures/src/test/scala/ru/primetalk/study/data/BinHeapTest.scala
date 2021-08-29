package ru.primetalk.study.data

import org.junit.Test
import org.junit.Assert
import org.junit.Assert.assertEquals
import ru.primetalk.study.data.binheap.BinHeap.BHeap
import ru.primetalk.study.data.binheap.BinHeapImpl
import ru.primetalk.study.data.laws.HeapLaws

class BinHeapTest:
  @Test def heapLawsTest =
    HeapLaws.HeapProperties[BHeap]("BinHeap")
      .check()

//    val results = org.scalacheck.Test.checkProperties(
//      org.scalacheck.Test.Parameters.default,
//      HeapLaws.heapProperties[BHeap]("BinHeap")
//    )
//    assert(results.forall(_._2.passed), results.filterNot(_._2.passed).mkString("\n"))
