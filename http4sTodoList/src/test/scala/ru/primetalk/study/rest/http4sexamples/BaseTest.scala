package ru.primetalk.study.rest.http4sexamples

import cats.effect.IO

class BaseTest:
  def assertIO[A](io: IO[A], expected: A): Unit =
    io
      .map(v =>
        assert(expected == v,
          s"expected $expected != received $v")
      )
      .unsafeRunSync()
