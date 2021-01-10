package ru.primetalk.study.fs2examples

import org.junit.Test
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.{io, text, Stream}
import java.io.InputStream
import cats.effect.unsafe.implicits.global

class TestLetterFreq:
    def resourceIO(name: String): IO[InputStream] = 
        IO{getClass.getResourceAsStream(name)}

    val largeFile: Stream[IO, Byte] =
        fs2.io.readInputStream(resourceIO("large-file.txt"),
        chunkSize = 4096, closeAfterUse = true)

    val wordRegEx = raw"[a-zA-Z]+".r

    def words: fs2.Pipe[IO, String, String] = 
        in => in.flatMap{ line => 
            Stream.emits(wordRegEx
                .findAllIn(line)
                .map(_.toString)
                .toList)
        }

    val largeFileWords = largeFile
        .through(text.utf8Decode)
        .through(text.lines)
        .through(words)

    case class Stat(charCount: Int, length: Int)

    def wordStat(char: Char)(word: String): Stat =
        Stat(word.count(_ == char), word.length)

    def combine(a: Stat, b: Stat): Stat =
        Stat(a.charCount + b.charCount, a.length + b.length)

    def freq(a: Stat): Double = a.charCount * 1.0 / a.length

    def freqIO(char: Char): IO[Double] = 
        largeFileWords
            .map(wordStat(char))
            .fold(Stat(0,0))(combine)
            .map(freq)
            .compile.toList.map(_.head)

    @Test def testLetterFreq: Unit =
        val freqA = freqIO('a').unsafeRunSync()
        val expectedFreq = 8.5/100 // https://www3.nd.edu/~busiforc/handouts/cryptography/letterfrequencies.html        
        assert(math.abs(freqA - expectedFreq) <= 0.01, s"freq = $freq")    
