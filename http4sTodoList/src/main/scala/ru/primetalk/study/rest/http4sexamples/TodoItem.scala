package ru.primetalk.study.rest.http4sexamples

import cats.Applicative
import fs2.Chunk
import io.circe.{Decoder, Encoder, HCursor, Json}

final case class TodoItem(text: String)

object TodoItem:
    
  given todoItemEncoder: Encoder[TodoItem] = new Encoder[TodoItem]:
    final def apply(a: TodoItem): Json = Json.obj(
      ("text", Json.fromString(a.text)),
    )

  given todoItemDecoder: Decoder[TodoItem] = new Decoder[TodoItem]:
    final def apply(c: HCursor): Decoder.Result[TodoItem] =
      for
        text <- c.downField("text").as[String] 
      yield 
        TodoItem(text)

//  import io.circe._, io.circe.generic.semiauto._
//  given decodeTodoItem: Decoder[TodoItem] = deriveDecoder[TodoItem]
//  given encodeTodoItem: Encoder.AsObject[TodoItem] = deriveEncoder[TodoItem]
//    Encoder.encodeString.contramap(_.text)
