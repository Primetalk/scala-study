package ru.primetalk.study.fs2examples

import org.junit.Test
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.{io, text, Stream}
import java.io.InputStream
import cats.effect.unsafe.implicits.global

class TestSenderQueue:

    @Test def testSenderQueue: Unit =
        val (sender, stream) = Sender[Int](1)
          .unsafeRunSync()// we have to run it preliminary to make `sender` available to external system
        
        val processing = 
            stream//.unNoneTerminate to send termination signal
                .map(i => i * i)
                .evalMap{ ii => IO{ println(ii)}}
        sender.send(1)

        val fiber = processing.compile.toList.start.unsafeRunSync()
        sender.send(2)
        Thread.sleep(100)
        (0 until 100).foreach(sender.send)
        println("finished")
