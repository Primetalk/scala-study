package ru.primetalk.study.data.binheap

object BinHeap2:
  sealed trait BinomialHeap[+T: Ordering, N <: Nat]
  case class Singleton[+T: Ordering](a: T) extends BinomialHeap[T, Zero]
  case class Link[+T: Ordering, N <: Nat](a: BinomialHeap[T, N], b: BinomialHeap[T, N]) extends BinomialHeap[T, Succ[N]]
  type BinHeap2[+T] = List[BinomialHeap[T, _]]
