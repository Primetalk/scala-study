package ru.primetalk.study.concurrency

import org.junit.Assert._
import org.junit.Test
import ru.primetalk.study.concurrency.Ints

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.{AtomicInteger, DoubleAdder}
import scala.concurrent.{Await, blocking}
import scala.util.Random

class TestJvmInts extends Ints:
  def avgOfFilteredInts(f: File, pred: Int => Boolean): Double =
    readInts(f)
      .filter(pred)
      .average

  @Test def testThreads(): Unit =

    class ThreadPredAverage(f: File, pred: Int => Boolean, result: Array[Double]) extends Thread("ThreadPredAverage"):

      override def run: Unit =
        result(0) = avgOfFilteredInts(f, pred)

    val avgEvenVar: Array[Double] = new Array[Double](1)
    val avgOddVar: Array[Double] = new Array[Double](1)
    val threadEven = new ThreadPredAverage(intsFile, isEven, avgEvenVar)
    val threadOdd = new ThreadPredAverage(intsFile, isOdd, avgOddVar)
    threadEven.start()
    threadOdd.start()
    threadEven.join()
    threadOdd.join()

    val avgEven = avgEvenVar(0)
    val avgOdd = avgOddVar(0)
    assertTrue(math.abs(avgEven - avgOdd) < evenOddThreshold)

  val ec = Executors.newCachedThreadPool()

  @Test def testRunnable(): Unit =
    val avgEvenVar: Array[Double] = new Array[Double](1)
    val avgOddVar: Array[Double] = new Array[Double](1)
    val taskEven = new Runnable:
      override def run(): Unit =
        avgEvenVar(0) = avgOfFilteredInts(intsFile, isEven)

    val taskOdd = new Runnable:
      override def run(): Unit =
        avgOddVar(0) = avgOfFilteredInts(intsFile, isOdd)
    
    val fEven = ec.submit(taskEven)
    val fOdd = ec.submit(taskOdd)
    fEven.get()
    fOdd.get()

    val avgEven = avgEvenVar(0)
    val avgOdd = avgOddVar(0)
    assertTrue(math.abs(avgEven - avgOdd) < evenOddThreshold)

  def readSumCountInts(f: File, pred: Int => Boolean): (Double, Int) =
    val ints = readInts(f)
    val filtered = ints.filter(pred)
    (1.0 * filtered.sum, filtered.length)

  @Test def testAtomic(): Unit =
    class Avg(pred: Int => Boolean):
      val lenVar: AtomicInteger = new AtomicInteger
      val sumVar: DoubleAdder = new DoubleAdder
      val task = new Runnable:
        override def run(): Unit =
          val (sum, len) = readSumCountInts(intsFile, pred)
          sumVar.add(sum)
          lenVar.set(len)

      def avg = sumVar.doubleValue() / lenVar.get()
    
    val even = new Avg(isEven)
    val odd = new Avg(isOdd)
    val fEven = ec.submit(even.task)
    val fOdd = ec.submit(odd.task)
    fEven.get()
    fOdd.get()
  
    val avgEven = even.avg
    val avgOdd = odd.avg
    assertTrue(math.abs(avgEven - avgOdd) < evenOddThreshold)
