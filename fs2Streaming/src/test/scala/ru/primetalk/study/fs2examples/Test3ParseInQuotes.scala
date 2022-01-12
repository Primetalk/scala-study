package ru.primetalk.study.fs2examples

import org.junit.Test
import fs2.Stream
import fs2.Pipe
import fs2.Pull
import fs2.Pure
import cats.effect.IO

class Test3ParseInQuotes:

  def parseInQuotes: Pipe[Pure, Char, String] =
    in => in//.fold((s, o2))(...)
      .mapAccumulate[Option[List[Char]], Option[String]](None: Option[List[Char]]){
        case (None, '\"') => 
          (Some(Nil), None)
        case (None, _) =>
          (None, None) // ignore chars outside
        case (Some(lst), '\"') => 
          (None, Some(lst.reverse.mkString("")))
        case (Some(lst), char) =>
          (Some(char::lst), None)
      }
      .collect{
        case (_, Some(str)) => str
      }

  @Test def testParseInQuotes: Unit =
    val input: Stream[Pure, Char] = Stream(
      """Ignore
        |"text1" some other ignore "text2"
        |"text3 which is not completed
        |""".stripMargin.toCharArray:_*)
    val res = 
      input
        .through(parseInQuotes)
        .compile.toList
    assert(res == List("text1", "text2"), s"res = $res")