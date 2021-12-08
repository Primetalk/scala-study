package ru.primetalk.study.concurrency

import org.junit.{Assert, Test}

import java.util.concurrent.{Exchanger, Executors}

class Test4Channels:
  val ec = Executors.newCachedThreadPool()

  @Test def testChannels: Unit =
    val ch1 = Channel[Int]()
    val ch2 = Channel[Int]()
    val inc: Runnable = () =>
      ch2 << (1 + <<(ch1))
    ec.submit(inc)
    ch1 << 10
    val res = <<(ch2)
    Assert.assertEquals(11, res)
