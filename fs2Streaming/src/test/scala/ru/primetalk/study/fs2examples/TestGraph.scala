package ru.primetalk.study.fs2examples

import akka.actor.ActorSystem
import akka.stream.{Inlet, Outlet, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Merge, Sink, Source}
import org.junit.Test
import ru.primetalk.synapse.core.{Contact, contact}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class TestGraph:
  @Test def testGraphAkka: Unit =
    import akka.stream.{ClosedShape, FlowShape}
    import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
    import akka._
    val flowSystem = GraphDSL.create[FlowShape[Int, Int]](){ implicit builder: GraphDSL.Builder[NotUsed] =>
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
    given ActorSystem = ActorSystem()
    val g: Future[Seq[Int]] = Source(1 to 3).via(flowSystem).runWith(sink)
    val res = Await.result(g, 1.second)
    assert(res == List(31, 31, 32, 32, 33, 33), s"res = $res")

  @Test def testGraphFs2: Unit =
    import fs2.{Pipe, Pure, Stream}
    import cats.effect.IO
    import cats.effect.std.Queue
    import cats.effect.unsafe.implicits.global
    def flowSystem(in: Stream[IO, Int]): Stream[IO, Int] =
      val f1: Pipe[IO, Int, Int] = _.map(_ + 10)
      val f2, f3, f4 = f1
      val mergeS = Stream.eval(Queue.unbounded[IO, Option[Int]])
      mergeS.flatMap {
        merge =>
          // noneTerminate - чтобы передать служебный сигнал окончания потока данных сквозь очередь.
          val f2withTermination: Pipe[IO, Int, Nothing] = f2.andThen(_.noneTerminate.evalMap(merge.offer).drain)
          val f4withTermination: Pipe[IO, Int, Nothing] = f4.andThen(_.noneTerminate.evalMap(merge.offer).drain)
          f1(in)
            .broadcastThrough(// broadcast - без отдельного компонента.
              f2withTermination,
              f4withTermination,
            )
            .merge(
              Stream.repeatEval(merge.take).unNoneTerminate // ловим сигнал завершения потока (None)
            )
            .through(f3)
      }
    val res = Stream(1,2,3).through(flowSystem).compile.toList.unsafeRunSync()
    assert(res == List(31, 31, 32, 32, 33, 33), s"res = $res")

  @Test def testGraphSynapseGrid: Unit =
    import ru.primetalk.synapse.core._
    import ru.primetalk.synapse._

    object FlowSystem extends BaseTypedSystem("FlowSystem"):
      val in: Contact[Int] = input[Int]("in")
      val out: Contact[Int] = output[Int]("out")
  
      override protected def defineSystem(implicit sb: SystemBuilder): Unit = {
        val bcast = contact[Int]("bcast")
        val merge = contact[Int]("merge")
        
        val f1, f2, f3, f4 = (i: Int) => i + 10

        LinkBuilderOps(in -> bcast).map(f1, "f1")
        LinkBuilderOps(bcast -> merge).map(f2, "f2")
        LinkBuilderOps(bcast -> merge).map(f4, "f4")
        LinkBuilderOps(merge -> out).map(f3, "f3")
      }
      def f: Int => Iterable[Int] = 
        this.toDynamicSystem.toTransducer(in, out)
  
    assert(FlowSystem.f(0) == List(30, 30))
    val res = (1 to 3).flatMap(FlowSystem.f)
    assert(res == Vector(31, 31, 32, 32, 33, 33), s"res = $res")

    FlowSystem.toStaticSystem.toDot(2).saveTo("FlowSystem.dot")
