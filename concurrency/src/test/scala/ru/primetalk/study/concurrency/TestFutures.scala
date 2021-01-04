package ru.primetalk.study.concurrency

import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future, duration}
import duration.DurationInt

class TestFutures extends Ints:
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  @Test def testFutures: Unit =
    val futureInts: Future[Array[Int]] = Future{
      loadInts()
    }
    def avg(pred: Int => Boolean): Future[Double] = futureInts.
      map{ ints =>
        1.0 * ints.filter(pred).sum / ints.length
      }
    
    val futureAvgEven = avg(_ % 2 == 0)
    val futureAvgOdd = avg(_ % 2 == 1)
    // NB. The above two futures have just started.
    val futureDiff = for {
      even <- futureAvgEven
      odd <- futureAvgOdd
    } yield
      math.abs(even - odd)

// Alternative implementation - starts the second future only after the first is completed
//    val futureDiff = for {
//      even <- avg(_ % 2 == 0)
//      odd <- avg(_ % 2 == 1)
//    } yield
//      math.abs(even - odd)
    val diff = Await.result(futureDiff, 10.seconds)
    assertTrue(diff < 1.0)
