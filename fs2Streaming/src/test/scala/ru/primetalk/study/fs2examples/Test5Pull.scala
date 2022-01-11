package ru.primetalk.study.fs2examples

import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.{Pull, Stream, io, text, _}
import org.junit.Test

import java.io.InputStream

class Test5Pull:

    extension [F[_], A](s: Stream[F, A])
        def myTake(n: Long): Stream[F, A] =
            def go(s: Stream[F, A], n: Long): Pull[F, A, Unit] = 
                if n <= 0 then // we may even skip reading the input when no output is needed
                    Pull.done
                else
                    s.pull.uncons
                        .flatMap {
                            case Some((headChunk,tailStream)) =>
                                headChunk.size match {
                                    case m if m < n => 
                                        Pull.output(headChunk) >> 
                                            go(tailStream, n - m)
                                    case m if m == n => 
                                        Pull.output(headChunk) >> 
                                            Pull.done
                                    case m => 
                                        Pull.output(headChunk.take(n.toInt)) >> 
                                            Pull.done
                                }
                            case None => 
                                Pull.done
                        }
            go(s, n).stream

    val nonNegativeInts = Stream.range(0, Int.MaxValue)

    @Test def testMyTake: Unit = 
        assert(nonNegativeInts.myTake(10).toList == nonNegativeInts.take(10).toList)
