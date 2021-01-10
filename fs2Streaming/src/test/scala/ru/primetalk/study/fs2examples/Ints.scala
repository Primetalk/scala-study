package ru.primetalk.study.fs2examples

import scala.util.Random

import fs2.Stream
import cats.effect.IO

trait Ints:
  def randomInts(r: Random, maxValue: Int): Stream[IO, Int] =
    Stream.eval(IO{r.nextInt(maxValue)}) ++ randomInts(r, maxValue)
