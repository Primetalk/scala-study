package ru.primetalk.study.data.binheap

import ru.primetalk.study.data.Heap
import ru.primetalk.study.data.binheap.BinHeap.BHeap

import scala.math.Ordering.Implicits.infixOrderingOps

object BinHeap:
  case class BinHeap[+T: Ordering](rank: Int, elem: T, list: List[BinHeap[T]])

  type BHeap[T] = List[BinHeap[T]]

given BinHeapImpl: Heap[BHeap] with
  def merge[T: Ordering](a: BHeap[T], b: BHeap[T]): BinHeap.BHeap[T] =
    (a, b) match
      case (Nil, b) => b
      case (a, Nil) => a
      case (ah :: at, bh :: bt) =>
        if ah.rank == bh.rank then
          val merged = link(ah, bh)
          val merged2 = merge(at, bt)
          merge(List(merged), merged2)
        else if ah.rank < bh.rank then
          ah :: merge(at, b)
        else
          bh :: merge(a, bt)

  def link[T: Ordering](a: BinHeap.BinHeap[T], b: BinHeap.BinHeap[T]): BinHeap.BinHeap[T] =
    assert(a.rank == b.rank)
    if a.elem < b.elem then
      BinHeap.BinHeap(a.rank + 1, a.elem, b :: a.list)
    else
      BinHeap.BinHeap(b.rank + 1, b.elem, a :: b.list)
    
  def insert[T: Ordering](a: BinHeap.BHeap[T], elem: T): BinHeap.BHeap[T] =
    merge(a, unit(elem))

  def findMinBinHeap[T: Ordering](a: BHeap[T]): Option[BinHeap.BinHeap[T]] =
    a.minByOption(_.elem)

  def findMin[T: Ordering](h: BinHeap.BHeap[T]): Option[T] =
    h.map(_.elem).minOption

  def deleteMin[T: Ordering](h: BHeap[T]): BHeap[T] =
    findMinBinHeap(h) match
      case Some(min) =>
        val elem = min.elem
        merge(h.filterNot(_.eq(min)), min.list)
      case None =>
        empty

  def empty[T: Ordering]: BinHeap.BHeap[T] =
    Nil

  def binUnit[T: Ordering](elem: T): BinHeap.BinHeap[T] =
    BinHeap.BinHeap(0, elem, Nil)

  def unit[T: Ordering](elem: T): BHeap[T] =
    List(binUnit(elem))

  def isEmpty[T: Ordering](h: BinHeap.BHeap[T]): Boolean =
    h.isEmpty

  def toListDesc[T: Ordering](h: BinHeap.BHeap[T], res: List[T] = Nil): List[T] =
    findMinBinHeap(h) match
      case Some(min) =>
        toListDesc(
          merge(h.filterNot(_.eq(min)), min.list.reverse),
          min.elem :: res
        )
      case None =>
        res
