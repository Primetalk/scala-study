package ru.primetalk.study.akka

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.junit.Test

/** Пример простой системы экторов из руководства akka
 * https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#per-session-child-actor
 */
class TestKeyWallet:

  @Test def testKeyWallet: Unit =
    // Ответы дочерних экторов
    case class Keys()
    case class Wallet()

    // Ответ эктора. Содержит экземпляры ключей и бумажника
    case class ReadyToLeaveHome(who: String, keys: Keys, wallet: Wallet)

    // Дочерний эктор. По запросу возвращает ключи
    object KeyCabinet:
      // запрос на получение ключей
      final case class GetKeys(whoseKeys: String, replyTo: ActorRef[Keys])

      def apply(): Behavior[GetKeys] =
        Behaviors.receiveMessage {
          case GetKeys(_, replyTo) =>
            Thread.sleep(100)
            // действие 6
            replyTo ! Keys()
            // replyTo ! Keys2Impl() 
            Behaviors.same
        }

    // // запрос на получение ключей
    // final case class GetKeys(whoseKeys: String, replyTo: ActorRef[Keys])

    // def KeyCabinetBehavior(): Behavior[GetKeys] =
    //   Behaviors.receiveMessage {
    //     case GetKeys(_, replyTo) =>
    //       Thread.sleep(100)
    //       replyTo ! Keys()
    //       // replyTo ! Keys2Impl() 
    //       Behaviors.same
    //   }

    // Дочерний эктор. По запросу возвращает бумажник
    object Drawer:
      case class GetWallet(whoseWallet: String, replyTo: ActorRef[Wallet])

      def apply(): Behavior[GetWallet] =
        Behaviors.receiveMessage {
          case GetWallet(_, replyTo) =>
            Thread.sleep(100)
            // действие 5
            replyTo ! Wallet()
            Behaviors.same
        }

    object Home:
      // командный внешний интерфейс эктора Home
      sealed trait Command
      case class LeaveHome(who: String, replyTo: ActorRef[ReadyToLeaveHome]) extends Command
//      private case class Response(r: ReadyToLeaveHome) extends Command// - скрытая часть интерфейса эктора
      

      
      def apply(): Behavior[Command] =
        Behaviors.setup[Command] { context =>
          val keyCabinet: ActorRef[KeyCabinet.GetKeys] = context.spawn(KeyCabinet(), "key-cabinet")
          val drawer: ActorRef[Drawer.GetWallet] = context.spawn(Drawer(), "drawer")

//          Behaviors.receiveSignal() - получение уведомлений от дочерних экторов о событиях жизненного цикла
          Behaviors.receiveMessage[Command] {
            case LeaveHome(who, replyTo) =>
              // Здесь мы создаём временного эктора, специально для выполнения возникшей задачи.
              // Эктор напрямую будет отправлять сформированный ответ тому эктору, который делал первоначальный запрос.
              // то есть наш эктор Home не узнает о том, что произошло.
              // действие 2
              context.spawn(prepareToLeaveHome(who, replyTo, keyCabinet, drawer), s"leaving-$who")
              Behaviors.same
          }
        }


      // per session actor behavior
      def prepareToLeaveHome(
                              whoIsLeaving: String,
                              replyTo: ActorRef[ReadyToLeaveHome],
                              keyCabinet: ActorRef[KeyCabinet.GetKeys],
                              drawer: ActorRef[Drawer.GetWallet]): Behavior[Nothing] =
        // we don't _really_ care about the actor protocol here as nobody will send us
        // messages except for responses to our queries, so we just accept any kind of message
        // but narrow that to more limited types when we interact
        Behaviors
          .setup[AnyRef] { context =>
            var wallet: Option[Wallet] = None
            var keys: Option[Keys] = None

            // we narrow the ActorRef type to any subtype of the actual type we accept
            // действие 3
            keyCabinet ! KeyCabinet.GetKeys(whoIsLeaving, context.self.narrow[Keys])
            // действие 4
            drawer ! Drawer.GetWallet(whoIsLeaving, context.self.narrow[Wallet])

            def nextBehavior(): Behavior[AnyRef] =
              (keys, wallet) match {
                case (Some(w), Some(k)) =>
                  // we got both, "session" is completed!
                  // действие 7
                  replyTo ! ReadyToLeaveHome(whoIsLeaving, w, k)
                  // действие 8
                  Behaviors.stopped

                case _ =>
                  Behaviors.same
              }

            Behaviors.receiveMessage {
              case w: Wallet =>
                wallet = Some(w)
                nextBehavior()
              case k: Keys =>
                keys = Some(k)
                nextBehavior()
              case _ =>
                Behaviors.unhandled
            }
          }
          .narrow[Nothing] // we don't let anyone else know we accept anything
          // то есть снаружи наш эктор не принимает никаких сообщений.

    // #per-session-child
    val testKit: ActorTestKit = ActorTestKit()
    val requestor = testKit.createTestProbe[ReadyToLeaveHome]()

    val home = testKit.spawn(Home(), "home")
    // действие 1
    home ! Home.LeaveHome("Bobby", requestor.ref)
    requestor.expectMessage(ReadyToLeaveHome("Bobby", Keys(), Wallet()))
