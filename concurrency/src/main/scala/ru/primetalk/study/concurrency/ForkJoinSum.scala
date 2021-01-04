package ru.primetalk.study.concurrency

import java.util.concurrent.RecursiveTask

class ForkJoinSum(ints: IndexedSeq[Int], from: Int, until: Int) extends RecursiveTask[Int]:
  override def compute(): Int =
    if until - from < 1 then
      0
    else if until - from == 1 then
      ints(from) // map identity
    else
      val count = (until - from) / 2
      val t1: ForkJoinSum = ForkJoinSum(ints, from, from + count)
      val t2: ForkJoinSum = ForkJoinSum(ints, from + count, until)
      t1.fork()
      t2.fork()
      t1.join() + t2.join() // reduce
