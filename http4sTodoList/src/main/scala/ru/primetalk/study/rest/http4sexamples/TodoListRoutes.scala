package ru.primetalk.study.rest.http4sexamples

import cats.{Applicative, Monad}
import cats.effect.{MonadThrow, Sync}
import org.http4s.circe._
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.syntax.all._
import io.circe.Encoder
import org.http4s.circe.jsonEncoderOf

import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

trait TodoListRoutes[F[_]]:
  val dsl = Http4sDsl[F]
  import dsl._

  def hello(text: String)(using Sync[F]): F[TodoItem] =
    TodoItem(text).pure[F]

  def todoListRoutes(using Sync[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "item" / name =>
        for
        greeting <- hello(name)
        resp <- Ok(greeting)
          yield
            resp
      case GET -> Root / "items" =>
        for
        items <- Storage.list[F]
        resp <- Ok(items)
          yield
            resp
    }
  def todoListModifyRoutes(using Sync[F], MonadThrow[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root /"item" =>
        for
          item <- req.as[TodoItem]
          _ <- Storage.prepend(item)
          resp <- Ok(item)
        yield
          resp
    }
