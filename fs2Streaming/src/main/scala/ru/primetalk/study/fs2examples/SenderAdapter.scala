package ru.primetalk.study.fs2examples

import cats.effect.{IO}
import cats.effect.unsafe.implicits.global
import cats.effect.std.Queue

import fs2.Stream

trait Sender[T]:
    def send(e: T): Unit

object Sender:
     def apply[T](bufferSize: Int): IO[(Sender[T], Stream[IO, T])] =
         for
             q <- Queue.bounded[IO, T](bufferSize)
         yield
             val sender: Sender[T] = (e: T) => q.offer(e).unsafeRunSync()
             def stm: Stream[IO, T] = Stream.eval(q.take) ++ stm
             (sender, stm)
