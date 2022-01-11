package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2._

class Test1Fib:
    def fib(prev: BigInt, b: BigInt): Stream[Pure, BigInt] = 
        Stream.emit(b) ++ fib(b, prev + b)

    val fib01 = fib(0, 1)
    @Test def testFib: Unit =
        assert(fib01.take(5).toList == List(1, 1, 2, 3, 5))

    @Test def testFib55: Unit =
        val res = fib01.drop(55).head.toList
        assert(res == List(BigInt(225_851_433_717L)), s"res = $res")
    
    @Test def testFibSquareOdd: Unit =
        assert(fib01
            .map(_ .pow(2))
            .filter(_ % 2 == 1)
            .take(5).toList == List(1, 1, 9, 25, 169)
        )
