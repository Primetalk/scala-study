package ru.primetalk.study.fs2examples

import akka.actor.ActorSystem
import akka.stream.{Inlet, Outlet, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Merge, Sink, Source}
import org.junit.Test

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class TestGraph:
  @Test def testGraphAkka: Unit =
    given ActorSystem = ActorSystem()
    import akka.stream.ClosedShape
    import akka.stream.FlowShape
    import akka.stream.scaladsl.{GraphDSL, RunnableGraph}
    import akka.stream.scaladsl.Flow
    import akka._
    val pipe = GraphDSL.create[FlowShape[Int, Int]](){ implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val bcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))
      val merge: UniformFanInShape[Int, Int] = builder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      val f1flow = builder.add(f1)
      val f3flow = builder.add(f3)
      
      f1flow.out ~> bcast 
      
      bcast ~> f2 ~> merge
      bcast ~> f4 ~> merge
      
      merge ~> f3flow.in
      
      FlowShape(f1flow.in, f3flow.out)
    }
    val sink: Sink[Int, Future[Seq[Int]]] = Sink.seq[Int]
    val g: Future[Seq[Int]] = Source(1 to 3).via(pipe).runWith(sink)
    val res = Await.result(g, 1.second)
    assert(res == List(31, 31, 32, 32, 33, 33), s"res = $res")

  @Test def testGraph: Unit =
    import fs2.{Pipe, Pure, Stream}
    import cats.effect.IO
    import cats.effect.std.Queue
    import cats.effect.unsafe.implicits.global
    def pipe: Pipe[IO, Int, Int] =
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
    val res = Stream(1,2,3).through(pipe).compile.toList.unsafeRunSync()
    assert(res == List(31, 31, 32, 32, 33, 33), s"res = $res")
