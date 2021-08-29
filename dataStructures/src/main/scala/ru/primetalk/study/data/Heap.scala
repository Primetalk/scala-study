package ru.primetalk.study.data

/** Type class of any heap implementation. */
trait Heap[H[*]]:
  def merge[T: Ordering](a: H[T], b: H[T]): H[T]

  def insert[T: Ordering](a: H[T], elem: T): H[T]

  def findMin[T: Ordering](h: H[T]): Option[T]

  def deleteMin[T: Ordering](h: H[T]): H[T]

  def empty[T: Ordering]: H[T]

  def unit[T: Ordering](elem: T): H[T]
  
  def isEmpty[T: Ordering](h: H[T]): Boolean

  def toListDesc[T: Ordering](h: H[T], res: List[T] = Nil): List[T]
  
object Heap:
  def fromList[H[*]: Heap, T: Ordering](lst: List[T]): H[T] =
    insertList(summon[Heap[H]].empty, lst)

  def insertList[H[*]: Heap, T: Ordering](a: H[T], lst: List[T]): H[T] =
    val h = summon[Heap[H]]
    lst.foldLeft(a)(h.insert)
