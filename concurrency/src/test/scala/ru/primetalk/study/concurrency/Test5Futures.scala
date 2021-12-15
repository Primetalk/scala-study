package ru.primetalk.study.concurrency

import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future, duration}
import duration.DurationInt

class Test5Futures extends Ints:
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  @Test def testFutures: Unit =
    val futureInts: Future[Array[Int]] = Future{
      loadInts()
    }
    def avg(pred: Int => Boolean)(using ExecutionContext): Future[Double] =
      futureInts
        .map(_.filter(pred))
        .map(_.average)
    
    val futureAvgEven = avg(isEven)
    val futureAvgOdd  = avg(isOdd)
    // NB. The above two futures have just started.
    val futureDiff: Future[Double] =
      // futureAvgEven.flatMap{evenAvg => avg(isOdd).map{ oddAvg => math.abs(evenAvg - oddAvg)}}
      for
        evenAvg <- futureAvgEven
        oddAvg  <- futureAvgOdd
      yield
        math.abs(evenAvg - oddAvg)

// Alternative implementation - starts the second future only after the first is completed
//    val futureDiff = for {
//      even <- avg(isEven)
//      odd  <- avg(_ % 2 == 1)
//    } yield
//      math.abs(even - odd)
    val diff = Await.result(futureDiff, 10.seconds)
    assertTrue(diff < evenOddThreshold)
