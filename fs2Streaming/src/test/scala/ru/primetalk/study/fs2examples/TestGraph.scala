package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2.{Pipe, Stream, Pure}
import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global

class TestGraph:
  /*
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 10)
    val out = Sink.ignore
  
    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))
  
    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)
  
    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
    bcast ~> f4 ~> merge
    ClosedShape
  })
   */
  @Test def testGraph: Unit =
    def g: Pipe[IO, Int, Int] =
      val f1: Pipe[IO, Int, Int] = _.map(_ + 10)
      val f2, f3, f4 = f1
      (in: Stream[IO, Int]) =>
        Stream.eval(Queue.unbounded[IO, Option[Int]]).flatMap {
          q =>
            f1(in)
              .broadcastTo(
                f2.andThen(_.noneTerminate.evalMap(q.offer).drain),
                f4.andThen(_.noneTerminate.evalMap(q.offer).drain),
              )
              .merge(Stream.repeatEval(q.take).unNoneTerminate.through(f3))
        }
    val res = Stream(1,2,3).through(g).compile.toList.unsafeRunSync()
    assert(res == List(31, 31, 32, 32, 33, 33), s"res = $res")
