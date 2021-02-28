package ru.primetalk.study.rest.akkahttpexamples

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import ru.primetalk.study.rest.akkahttpexamples.TodoItemRegistry.{ActionPerformed, CreateTodoItem, GetTodoItems}
import spray.json.DefaultJsonProtocol

class TodoItemsRoutes(itemsRegistry: ActorRef[TodoItemRegistry.Command])(implicit val system: ActorSystem[_]):

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import DefaultJsonProtocol.listFormat
  import JsonFormats._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout = Timeout.create(java.time.Duration.ofSeconds(5))//system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getItems(): Future[List[TodoItem]] =
    itemsRegistry.ask(GetTodoItems(_))
  def createItem(item: TodoItem): Future[ActionPerformed] =
    itemsRegistry.ask(CreateTodoItem(item, _))

  val itemsRoutes: Route = 
    concat(
      pathPrefix("items") {
        pathEnd {
          get {
            complete(StatusCodes.OK, getItems())
          }
        }
      },
      pathPrefix("item") {
        pathEnd {
          post {
            entity(as[TodoItem]) { item =>
              onSuccess(createItem(item)) { performed =>
                complete(StatusCodes.Created, performed)
              }
            }
          }
        }
      }
    )
