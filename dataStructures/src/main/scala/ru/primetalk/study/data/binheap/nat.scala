package ru.primetalk.study.data.binheap

/**
 * @see https://dotty.epfl.ch/docs/reference/metaprogramming/inline.html
 */
sealed trait Nat
case object Zero extends Nat
case class Succ[N <: Nat](n: N) extends Nat
type Zero = Zero.type

transparent inline def toInt(n: Nat, from: Int = 0): Int =
  inline n match
    case Zero     => from
    case Succ(n1) => toInt(n1, from + 1)

//inline val natTwo = toInt(Succ(Succ(Zero)))
//val intTwo: 2 = natTwo
