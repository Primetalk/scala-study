package ru.primetalk.study.rest.http4sexamples

import org.junit.Test
import cats.effect.IO
import org.http4s.syntax.all._
import org.http4s._
import org.http4s.Method.{GET}

class TodoItemTest extends BaseTest:
    @Test
    def textTest: Unit =
        assert(TodoItem("Hello").text == "Hello")

    @Test
    def routesTest: Unit =
        assertIO(retItems.map(_.status), Status.Ok)
        assertIO(retItems.flatMap(_.as[String]), """[{"text":"Do work"}]""")
    
    val retItems: IO[Response[IO]] =
        val getItems = Request[IO](GET, Uri.fromString("/items").getOrElse(???))
        val routes = new TodoListRoutes[IO]{}.todoListRoutes
        routes.orNotFound(getItems)
