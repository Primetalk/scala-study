package ru.primetalk.study.concurrency

import org.junit.Test
import org.junit.Assert._
import cats.effect.IO

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

class TestIO extends ru.primetalk.study.concurrency.Ints:

  val processorCount = Runtime.getRuntime().availableProcessors()
  
  val ioInts: IO[Array[Int]] = IO{loadInts()}
  
  @Test def testIO: Unit =
    def avg(pred: Int => Boolean): IO[Double] = 
      ioInts 
        .map{ints => 
          1.0 * ints.filter(pred).sum / ints.length
        }
    val ioAvgEven = avg(_ % 2 == 0) // NB! ioInts is executed twice
    val ioAvgOdd = avg(_ % 2 == 1)
    val ioDiff = for {
      even <- ioAvgEven
      odd <- ioAvgOdd
    } yield
      math.abs(even - odd)
    val diff = ioDiff.unsafeRunSync() // single thread execution!
    assertTrue(diff < 1.0)

  @Test def testIO2: Unit =
    def avg(pred: Int => Boolean)(ints: Array[Int]): IO[Double] =
      IO{
        1.0 * ints.filter(pred).sum / ints.length
      }
      
    val ioAvgEven = avg(_ % 2 == 0)(_)
    val ioAvgOdd = avg(_ % 2 == 1)(_)
    val ioDiff = for {
      ints <- ioInts // NB! ioInts is executed once
      even <- ioAvgEven(ints) 
      odd <- ioAvgOdd(ints)
    } yield
      math.abs(even - odd)
    val diff = ioDiff.unsafeRunSync()
    assertTrue(diff < 1.0)

  // Needed for IO.start to do a logical thread fork
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cpuPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def cpuEval[A](ioa: IO[A]): IO[A] =
    ioa.evalOn(cpuPool)

  @Test def testIOAsync: Unit =
    def avg(pred: Int => Boolean)(ints: Array[Int]): IO[Double] = IO{
      1.0 * ints.filter(pred).sum / ints.length
    }
      
    val ioAvgEven = avg(_ % 2 == 0)(_)
    val ioAvgOdd = avg(_ % 2 == 1)(_)
    val ioDiff = for {
      ints <- ioInts
      even <- cpuEval(ioAvgEven(ints))
      odd <- cpuEval(ioAvgOdd(ints))
    } yield
      math.abs(even - odd)
    // Until here nothing is even started.
    val diff = ioDiff.unsafeRunSync()
    assertTrue(diff < 1.0)

