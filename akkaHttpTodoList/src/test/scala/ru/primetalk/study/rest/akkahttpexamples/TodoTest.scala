package ru.primetalk.study.rest.akkahttpexamples

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import org.junit.Test
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTest, ScalatestRouteTest, ScalatestUtils, TestFrameworkInterface}
import org.scalactic.source
import org.scalatest.{Args, Status, Suite, TestSuite}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funspec.{AnyFunSpec, AnyFunSpecLike}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.AnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec

class TodoSuite extends AnyFunSuite with Matchers with ScalaFutures with ScalatestRouteTest:
  // workaround for incompatible versions of scalatest and akka
  override def run(testName: Option[String], args: Args): Status = super[ScalatestRouteTest].run(testName, args)
  implicit val pos: source.Position = source.Position("TodoSuite.scala", "./", 19)// scalatest macro workaround
  
  lazy val testKit = ActorTestKit()

  implicit def typedSystem: ActorSystem[Nothing] = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  test("TodoListServer"){
    val todoItemRegistry = testKit.spawn(TodoItemRegistry())
    lazy val routes = new TodoItemsRoutes(todoItemRegistry).itemsRoutes
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import JsonFormats._
    val request = HttpRequest(uri = "/items")
    request ~> routes ~> check {
      status should === (StatusCodes.OK) 
      // we expect the response to be json:
      contentType should === (ContentTypes.`application/json`)
      // and no entries should be in the list:
      entityAs[String] should === ("""[{"text":"Do work"}]""")
    }
  }
