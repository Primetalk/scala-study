package ru.primetalk.study.rest.akkahttpexamples

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success

object TodoListServerApp:

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit =

    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8040).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

  def main(args: Array[String]): Unit =
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val itemsRegistryActor = context.spawn(TodoItemRegistry(), "TodoListRegistryActor")
      context.watch(itemsRegistryActor)

      val routes = new TodoItemsRoutes(itemsRegistryActor)(context.system)
      startHttpServer(routes.itemsRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "TodoListAkkaHttpServer")
