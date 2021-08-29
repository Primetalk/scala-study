package ru.primetalk.study.data

import org.junit.Test
import org.junit.Assert
import org.junit.Assert.assertEquals
import ru.primetalk.study.data.binheap.BinHeap.BHeap
import ru.primetalk.study.data.binheap.BinHeapImpl
import ru.primetalk.study.data.laws.HeapLaws
import ru.primetalk.study.data.leftheap.LeftHeap
import ru.primetalk.study.data.leftheap.HeapImpl

class LeftHeapTest:
  val a0 = HeapImpl.unit(0)
  val a1 = HeapImpl.unit(1)
  val a2 = HeapImpl.unit(2)

  @Test def minTest =
    assertEquals(HeapImpl.merge(a1, a2).findMin, Some(1))
    assertEquals(HeapImpl.merge(a2, a1).findMin, Some(1))
    assertEquals(HeapImpl.merge(a0, HeapImpl.merge(a1, a2)).findMin, Some(0))
    assertEquals(HeapImpl.merge(HeapImpl.merge(a0, a1), a2).findMin, Some(0))

  @Test def insertTest =
    assertEquals(HeapImpl.insert(a1, 0), HeapImpl.insert(a0, 1))

  @Test def toListTest =
    assertEquals(HeapImpl.merge(a0, HeapImpl.merge(a1, a2)).toList, List(2,1,0))
    assertEquals(HeapImpl.merge(HeapImpl.merge(a0, a1), a2).toList, List(2,1,0))

  @Test def leftHeapLawsTest =
    HeapLaws.HeapProperties[LeftHeap]("LeftHeap")
      .check()
