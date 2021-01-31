package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global

class TestMerge:

  @Test def testMerge: Unit =
    val stream1 = Stream(1,2,3)
    val stream2 = Stream.eval(IO {
      Thread.sleep(200)
      4
    })
    val combinedStream = stream1.merge(stream2)
    val result = combinedStream.compile.toVector.unsafeRunSync()
    assert(result == Vector(1,2,3,4))
  
  @Test def testMerge2: Unit =
    val stream1: Stream[IO, Int] = Stream[IO, Int](1,2,3)
    val stream2: Stream[IO, Int] = Stream.eval(IO {
      Thread.sleep(1000)
      4
    })
    val combinedStream: Stream[IO, Int] = stream1.mergeHaltBoth(stream2)
    val result = combinedStream.compile.toVector.unsafeRunSync()
    assert(result == Vector(1,2,3), s"result = $result")
