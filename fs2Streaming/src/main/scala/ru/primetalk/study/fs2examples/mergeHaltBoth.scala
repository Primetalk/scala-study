package ru.primetalk.study.fs2examples

import fs2.{Stream, Pull}
import cats.effect.IO

type Pipe2[F[_],-I,-I2,+O] = (Stream[F,I], Stream[F,I2]) => Stream[F,O]

/** Like `merge`, but halts as soon as _either_ branch halts. */
def mergeHaltBoth[O]: Pipe2[IO,O,O,O] = (s1, s2) => {
  def go(s1: Stream[IO, O], s2: Stream[IO, O]): Pull[IO, O, Unit] =
    s1.pull.uncons.flatMap{
      case None =>
        Pull.done
      case Some((headChunk1, tailStream1)) =>
        Pull.output(headChunk1) >>
          s2.pull.uncons.flatMap{
            case None =>
              Pull.done
            case Some((headChunk2, tailStream2)) =>
              Pull.output(headChunk2) >>
                go(tailStream1, tailStream2)
          }
    }
  go(s1, s2).stream
}
