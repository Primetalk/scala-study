package ru.primetalk.study.concurrency

import java.util.concurrent.RecursiveTask

class ForkJoinMapReduce[T, R](values: IndexedSeq[T], from: Int, until: Int,
                              map: T => R,
                              reduce: (R, R) => R)extends RecursiveTask[R]:
  override def compute(): R =
    if until - from < 1 then
      throw IllegalArgumentException("Cannot reduce empty")
    else if until - from == 1 then
      map(values(from))
    else
      val count = (until - from) / 2
      val t1: ForkJoinMapReduce[T, R] = ForkJoinMapReduce(values, from, from + count, map, reduce)
      val t2: ForkJoinMapReduce[T, R] = ForkJoinMapReduce(values, from + count, until, map, reduce)
      t1.fork()
      t2.fork()
      reduce(t1.join(), t2.join())
