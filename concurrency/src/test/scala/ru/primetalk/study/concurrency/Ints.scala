package ru.primetalk.study.concurrency

import java.io.File
import java.nio.file.Files
import scala.util.Random

trait Ints:
  def randomInts(r: Random, maxValue: Int): LazyList[Int] =
    r.nextInt(maxValue) #:: randomInts(r, maxValue)

  def createTempFileOfInts(ints: LazyList[Int]): File =
    val content = ints
      .map(_.toString)
      .mkString("\n")
    val f = File.createTempFile("ints_", ".txt")
    Files.writeString(f.toPath, content)
    f

  lazy val intsFile =
    val ints = randomInts(new Random(0), 1000)
      .take(1_000_000)
    createTempFileOfInts(ints)

  def readInts(f: File): Array[Int] =
    Files
      .readString(f.toPath)
      .split('\n')
      .map(_.toInt)
      
  def loadInts(): Array[Int] = 
    readInts(intsFile)
