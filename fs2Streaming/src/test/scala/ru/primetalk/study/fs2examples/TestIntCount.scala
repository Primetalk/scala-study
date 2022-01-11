package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2.Stream
import fs2.Pipe
import fs2.Pull
import fs2.Pure
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.util.Random

class TestIntCount:
  /** Генерируем одно случайное значение. 
    * При этом происходит побочный эффект - изменяется r: Random.
    * Поэтому при повторных вызовах - будут генерироваться новые числа.
    */
  def randomInt(r: Random, maxValue: Int): IO[Int] =
    IO{r.nextInt(maxValue)}
  /** Создаём поток одинаковых вычислений. */
  def stream[A](io: IO[A]): Stream[IO, A] =
    Stream.eval(io) ++ stream(io)
  def stream1[A](io: IO[A]): Stream[IO, A] =
    Stream.eval(io).repeat
  def stream2[A](io: IO[A]): Stream[IO, A] =
    Stream.repeatEval(io)
  
  val digits = stream(randomInt(new Random(0), 10))
  
  @Test def testIntCount: Unit =
    val partialCounts = digits.mapAccumulate(Map[Int, Int]()){
      case (map, digit) =>
        val map2 = map.updated(digit, map.getOrElse(digit, 0) + 1)
        (map2, ())
    }
    val map = partialCounts.drop(100).take(1).compile.toList.unsafeRunSync().head._1
    assert(map == Map(0 -> 8, 5 -> 13, 1 -> 9, 6 -> 7, 9 -> 5, 2 -> 12, 7 -> 17, 3 -> 11, 8 -> 13, 4 -> 6), s"map = $map")
