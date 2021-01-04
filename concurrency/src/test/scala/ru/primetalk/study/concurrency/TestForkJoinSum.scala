package ru.primetalk.study.concurrency

import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.ForkJoinPool
import scala.util.Random

class TestForkJoinSum extends Ints:
  
  val ints = randomInts(new Random(0), 1000)
    .take(1_000_000)
    .toIndexedSeq
  
  val expected = ints.sum
  
  @Test def testForkJoinSum: Unit =
    val t = new ForkJoinSum(ints, 0, ints.size)
    val pool = new ForkJoinPool()
    val sum = pool.invoke(t)
    assertEquals(expected, sum)
  
  @Test def testForkJoinMapReduce: Unit =
    val t = new ForkJoinMapReduce(ints, 0, ints.size, identity, _ + _)
    val pool = new ForkJoinPool()
    val sum = pool.invoke(t)
    assertEquals(expected, sum)
