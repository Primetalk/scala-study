package ru.primetalk.study.akka

import org.junit.Test
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SpawnProtocol}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class PrivateProtocolExample:
  // Тип, доступный извне
  sealed trait PublicProtocol
  case object Start extends PublicProtocol

  object TwoProtocolActor:
    sealed trait PrivateProtocol
    // реализация типа с ограниченной видимостью.
    private case class PrivateProtocolImpl() extends PrivateProtocol
    def apply(): Behavior[PublicProtocol] = 
      Behaviors
      .setup[PrivateProtocol | PublicProtocol]{ context =>
        val inner = context.spawn(InnerActor.apply(), "inner")
        // создать вспомогательного эктора
        // этот эктор будет оправлять назад экземпляр LightOff
        Behaviors.receiveMessage{
          case Start =>
            inner ! InnerActor.InnerActorMessage(context.self.narrow)
            Behaviors.same
          case PrivateProtocolImpl() =>
            println("Inner actor responded")
            Behaviors.stopped
        }
      }
      .narrow

    object InnerActor:
      case class InnerActorMessage(reportTo: ActorRef[PrivateProtocol])
      def apply(): Behavior[InnerActorMessage] = 
        Behaviors.receive{
          case (context, InnerActorMessage(act)) =>
            act ! PrivateProtocolImpl()
            Behaviors.stopped
        }

  @Test def testTwoProtocolActor: Unit =
    val twoProtocolActor: ActorSystem[PublicProtocol] = ActorSystem(TwoProtocolActor(), "TwoProtocolActor")

    twoProtocolActor ! Start  // первое сообщение начнёт процесс
    Await.result(twoProtocolActor.whenTerminated, 1.second) // ждём пока система самозавершится (без ошибок)
