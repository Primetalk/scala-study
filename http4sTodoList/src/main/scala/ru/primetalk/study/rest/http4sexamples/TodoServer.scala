package ru.primetalk.study.rest.http4sexamples

import cats.effect.{IO, IOApp, ExitCode}
import cats.effect.syntax._
import org.http4s.server.blaze.BlazeServerBuilder
import cats.implicits._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

object TodoServer extends IOApp with TodoListRoutes[IO] with Config:
  
  val app = (
    todoListRoutes <+>
    todoListModifyRoutes
    ).orNotFound
  
  val server = BlazeServerBuilder[IO](global)
    .bindHttp(port)
    .withHttpApp(app)
    
  val serverResource = server.resource
  // if we want to run server in parallel:
  //  val fiber = serverResource.use(_ => IO.never).start.unsafeRunSync()
  
  def run(args: List[String]): IO[ExitCode] =
    server
      .serve
      .compile.drain
      .as(ExitCode.Success)  
