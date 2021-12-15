package ru.primetalk.study.data.leftheap

import ru.primetalk.study.data.Heap

import scala.math.Ordering.Implicits.infixOrderingOps

/**
 * Heap example is from Dr. Chris Okasaki "Purely functional data structures", 1998.
 * https://www.cs.cmu.edu/~rwh/theses/okasaki.pdf
 */
sealed trait LeftHeap[+T: Ordering]:
  def rank: Int = 
    this match
      case LeftHeap.Empty => 0
      case LeftHeap.LeftHeap1(r, _, _, _) => r

  def findMin: Option[T] = 
    this match
      case LeftHeap.Empty => None
      case LeftHeap.LeftHeap1(_, elem, _, _) => Some(elem)

  def deleteMin: LeftHeap[T] = 
    this match
      case LeftHeap.Empty => LeftHeap.Empty
      case LeftHeap.LeftHeap1(_, _, a, b) => HeapImpl.merge(a, b)

  def toRevList: List[T] = 
    this match
      case LeftHeap.Empty => Nil
      case LeftHeap.LeftHeap1(_, elem, a, b) => elem :: HeapImpl.merge(a, b).toRevList
  
  def prependToList[T0 >: T](lst: List[T0]): List[T0] = 
    this match 
      case LeftHeap.Empty => lst
      case LeftHeap.LeftHeap1(_, elem, a, b) => HeapImpl.merge(a, b).prependToList(elem :: lst)
  
  def toList: List[T] = 
    prependToList(Nil)

object LeftHeap:
  case object Empty extends LeftHeap[Nothing]

  case class LeftHeap1[+T: Ordering](r: Int, elem: T, left: LeftHeap[T], right: LeftHeap[T]) extends LeftHeap[T]

  def makeHeap1[T: Ordering](elem: T, a: LeftHeap[T], b: LeftHeap[T]): LeftHeap[T] =
    if a.rank >= b.rank then
      LeftHeap1(b.rank + 1, elem, a, b)
    else
      LeftHeap1(a.rank + 1, elem, b, a)

given HeapImpl: Heap[LeftHeap] with
  def merge[T: Ordering](a: LeftHeap[T], b: LeftHeap[T]): LeftHeap[T] = 
    (a, b) match 
      case (LeftHeap.Empty, b) => b
      case (a, LeftHeap.Empty) => a
      case (LeftHeap.LeftHeap1(r1, elem1, left1, right1), LeftHeap.LeftHeap1(r2, elem2, left2, right2)) =>
        if elem1 >= elem2 then
          LeftHeap.makeHeap1(elem2, left2, merge(a, right2))
        else
          LeftHeap.makeHeap1(elem1, left1, merge(right1, b))
  def insert[T: Ordering](a: LeftHeap[T], elem: T): LeftHeap[T] =
    merge(a, unit(elem))

  def findMin[T: Ordering](h: LeftHeap[T]): Option[T] = h.findMin

  def deleteMin[T: Ordering](h: LeftHeap[T]): LeftHeap[T] = h.deleteMin

  def empty[T: Ordering]: LeftHeap[T] = LeftHeap.Empty

  def unit[T: Ordering](elem: T): LeftHeap[T] =
    LeftHeap.LeftHeap1(1, elem, LeftHeap.Empty, LeftHeap.Empty)

  def isEmpty[T: Ordering](h: LeftHeap[T]): Boolean = 
    h == LeftHeap.Empty
    
  def toListDesc[T: Ordering](h: LeftHeap[T], res: List[T] = Nil): List[T] =
    h match
      case LeftHeap.Empty => 
        res
      case LeftHeap.LeftHeap1(_, elem, a, b) => 
        toListDesc(HeapImpl.merge(a, b), elem :: res)
