package ru.primetalk.study.concurrency

import org.junit.Assert._
import org.junit.Test

import java.util.concurrent.Executors

/** 
 * NB! Not recommended!
 * For volatile demonstration purposes only.
 */
class Test2JvmVolatile:
  val ec = Executors.newCachedThreadPool()

  @Test def testVolatile(): Unit =
    @volatile
    var flag: Boolean = false
    var simpleVar: Long = 0L
    @volatile
    var completed: Boolean = false
    val updateSimpleVar: Runnable = () =>
      var i = 0
      while(i < 500)
        flag = false
        Thread.sleep(1)
        simpleVar = -1L
        Thread.sleep(1)
        simpleVar = 0L
        flag = true
        i += 1
      completed = true
    var sum: Long = 0L
    @volatile
    var sumIsReady: Boolean = false 
    val checkSimpleVar: Runnable = () =>
      var i = 0
      while(!completed)
        if flag then sum += simpleVar
      sumIsReady = true
        
    val f1 = ec.submit(updateSimpleVar)
    ec.submit(checkSimpleVar)
    f1.get()
    while(!sumIsReady) Thread.sleep(1)
    assertEquals(0L, sum)
