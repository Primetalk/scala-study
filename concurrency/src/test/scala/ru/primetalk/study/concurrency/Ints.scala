package ru.primetalk.study.concurrency

import java.io.File
import java.nio.file.Files
import scala.util.Random

trait Ints:
  extension (r: Random)
    def randomInts(maxValue: Int): LazyList[Int] =
      r.nextInt(maxValue) #:: r.randomInts(maxValue)

  def createTempFileOfInts(ints: LazyList[Int]): File =
    val content = ints
      .map(_.toString)
      .mkString("\n")
    val f = File.createTempFile("ints_", ".txt")
    Files.writeString(f.toPath, content)
    f

  lazy val intsFile: File =
    val ints = new Random(0).randomInts(1000)
      .take(1_000_000)
    createTempFileOfInts(ints)

  /** Slow IO from a real file. */
  def readInts(f: File): Array[Int] =
    Files
      .readString(f.toPath)
      .split('\n')
      .map(_.toInt)
      
  def loadInts(): Array[Int] = 
    readInts(intsFile)

  val isEven: Int => Boolean = _ % 2 == 0
  val isOdd:  Int => Boolean = _ % 2 == 1

  extension (arr: Array[Int]) 
    def average: Double = 1.0 * arr.sum / arr.length

  val evenOddThreshold = 2.0
