package ru.primetalk.study.rest.akkahttpexamples

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

object TodoItemRegistry:
  // protocol
  sealed trait Command
  final case class GetTodoItems(replyTo: ActorRef[List[TodoItem]]) extends Command
  final case class CreateTodoItem(item: TodoItem, replyTo: ActorRef[ActionPerformed]) extends Command

  // responses
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(items: Set[TodoItem]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetTodoItems(replyTo) =>
        replyTo ! items.toList
        Behaviors.same
      case CreateTodoItem(item, replyTo) =>
        replyTo ! ActionPerformed(s"TodoItem '${item.text}' created.")
        registry(items + item)
    }
