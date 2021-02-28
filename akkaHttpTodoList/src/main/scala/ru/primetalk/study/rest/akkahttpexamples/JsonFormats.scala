package ru.primetalk.study.rest.akkahttpexamples

import TodoItemRegistry.ActionPerformed

//#json-formats
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val todoItemJsonFormat: RootJsonFormat[TodoItem] = jsonFormat1(TodoItem(_))
  implicit val todoItemsJsonFormat: RootJsonFormat[List[TodoItem]] = DefaultJsonProtocol.listFormat


  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed(_))
}
