package ru.primetalk.study.rest.http4sexamples

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s._
import org.http4s.client._
import org.http4s.blaze.client.BlazeClientBuilder
import scala.concurrent.ExecutionContext.global
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import io.circe.syntax._
import org.http4s.circe._

object TodoClient extends IOApp with Config:
  def run(args: List[String]): IO[ExitCode] =    
    BlazeClientBuilder[IO](global).resource.use { client =>
      val item = TodoItem("Do some other work")
      val itemApi = Uri.fromString(baseUrl + "/item").getOrElse(???)
      val postTodoItem = POST(
        item,
        itemApi
      )
      
      for
        status <- client.status(postTodoItem)
        _ <- IO{println(s"Post status: $status")}
        items <- client.expect[List[TodoItem]](baseUrl+"/items")
        _ <- IO{ assert(items.exists(_.text.contains("other"))) }
      yield
        ExitCode.Success
    }
