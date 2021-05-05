package ru.primetalk.study.concurrency

import org.junit.Test
import org.junit.Assert._
import cats.effect.IO

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

class TestIO extends Ints:

  val processorCount = Runtime.getRuntime().availableProcessors()
  
  val ioInts: IO[Array[Int]] = IO{loadInts()}
  
  @Test def testIO: Unit =
    def avg(pred: Int => Boolean): IO[Double] = 
      ioInts 
        .map(_.filter(pred))
        .map(_.average)

    val ioAvgEven = avg(isEven) // NB! ioInts is executed twice
    val ioAvgOdd  = avg(isOdd)
    val ioDiff =
      for
        even <- ioAvgEven
        odd  <- ioAvgOdd
      yield
        math.abs(even - odd)
    val diff = ioDiff.unsafeRunSync() // single thread execution!
    assertTrue(diff < evenOddThreshold)

  def filteredAverage(pred: Int => Boolean)(ints: Array[Int]): IO[Double] =
    IO{
      ints
        .filter(pred)
        .average
    }

  @Test def testIO2: Unit =

    val ioAvgEven = filteredAverage(isEven)(_)
    val ioAvgOdd  = filteredAverage(isOdd)(_)
    val ioDiff =
      for
        ints <- ioInts // NB! ioInts is executed once
        even <- ioAvgEven(ints)
        odd <- ioAvgOdd(ints)
      yield
        math.abs(even - odd)
    val diff = ioDiff.unsafeRunSync()
    assertTrue(diff < evenOddThreshold)

  // Needed for IO.start to do a logical thread fork
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cpuPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def cpuEval[A](ioa: IO[A]): IO[A] =
    ioa.evalOn(cpuPool)

  @Test def testIOAsync: Unit =
    val ioAvgEven = filteredAverage(isEven)(_)
    val ioAvgOdd = filteredAverage(isOdd)(_)
    val ioDiff = for {
      ints <- ioInts
      even <- cpuEval(ioAvgEven(ints))
      odd  <- cpuEval(ioAvgOdd(ints))
    } yield
      math.abs(even - odd)
    // Until here nothing is even started.
    val diff = ioDiff.unsafeRunSync()
    assertTrue(diff < evenOddThreshold)
