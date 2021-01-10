package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2.Stream
import fs2.Pipe
import fs2.Pull
import fs2.Pure
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.util.Random

class TestIntCount extends Ints:
  @Test def testIntCount: Unit =
    val digits = randomInts(new Random(0), 10)
    val partialCounts = digits.mapAccumulate(Map[Int, Int]()){
      case (map, digit) =>
        val map2 = map.updated(digit, map.getOrElse(digit, 0) + 1)
        (map2, ())
    }
    val map = partialCounts.drop(100).take(1).compile.toList.unsafeRunSync().head._1
    assert(map == Map(0 -> 8, 5 -> 13, 1 -> 9, 6 -> 7, 9 -> 5, 2 -> 12, 7 -> 17, 3 -> 11, 8 -> 13, 4 -> 6), s"map = $map")
