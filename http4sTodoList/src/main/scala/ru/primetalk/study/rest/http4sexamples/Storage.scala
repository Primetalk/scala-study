package ru.primetalk.study.rest.http4sexamples

import cats.Defer
import cats.effect.Sync
import cats.syntax.all._

object Storage:
  private var items: List[TodoItem] = List(TodoItem("Do work"))

  def list[F[_]](using Sync[F]): F[List[TodoItem]] =
    items.pure

  def prepend[F[_]: Sync](item: TodoItem): F[Unit] = Sync[F].pure{ 
    items = item :: items 
  }
  
  def sort[F[_]: Sync]: F[Unit] = { 
    items = items.sortBy(_.text)
  }.pure
