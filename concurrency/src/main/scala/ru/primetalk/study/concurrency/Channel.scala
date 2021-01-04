package ru.primetalk.study.concurrency

import java.util.concurrent.SynchronousQueue

class Channel[A]:
  private val queue = new SynchronousQueue[A]

  def send(v: A): Unit =
    queue.put(v)

  def receive: A =
    queue.take()

  def << (v: A) = send(v)

def <<[A](channel: Channel[A]): A = channel.receive
