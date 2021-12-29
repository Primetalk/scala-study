package ru.primetalk.study.akka

import akka.NotUsed
import org.junit.Test
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SpawnProtocol}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/** 
 * Два простых эктора, обменивающихся сообщениями пинг/понг.
 * Акторы идентичные. Отличаются только в процессе создания конфигурации.
 */
class TestSimpleActors:
  /** Сообщения, которыми обмениваются акторы */
  sealed trait Ball
  
  /** @param from - обратный адрес, куда следует направлять ответы. */
  final case class Ping(count: Int, from: ActorRef[Ball]) extends Ball
  final case class Pong(count: Int, from: ActorRef[Ball]) extends Ball
//  final case class BallImpl[P](count: Int, from: ActorRef[Ball]) extends Ball

  /** Мы хотим закончить обмен через $maxCount шагов. */
  val maxCount = 100
  
  /** Собственно логика актора, который может принимать сообщения Ball */
  def pingPong: Behavior[Ball] = 
    Behaviors.receive {
      // Вместе с сообщением мы принимаем контекст, дающий доступ к внутренностям актора.
      case (context, Ping(count, from)) => //        context.
        println(s"Ping($count)")
        from ! Pong(count + 1, context.self) // передаём ссылку на себя, чтобы ответы поступали обратно
//        if count == 100 then pingPongAlternativeBehavior else
        Behaviors.same
      case (context, Pong(count, from)) =>
        if count < maxCount then 
          from ! Ping(count + 1, context.self)
        else
          println("Completed")// разрываем цикл, путём отказа от поддержания диалога.
        Behaviors.same
    }
  def pingPongAlternativeBehavior: Behavior[Ball] = ???
  /** Корневой актор всей системы. 
   * NotUsed - что-то вроде Unit'а. 
   * Используется для совместимости с Java. */
  def guard(): Behavior[NotUsed] = 
    // Удобный способ отделить получение контекста от обработки сообщений.
    Behaviors.setup { context =>
      
      // контекст позволяет запускать новых акторов
      val p1: ActorRef[Ball] = context.spawn(pingPong, "p1")
      // имена должны быть уникальными.
      val p2: ActorRef[Ball] = context.spawn(pingPong, "p2")
      
      Behaviors.receiveMessage { _ => // запустить обмен можно любым сообщением.
        p1 ! Ping(0, p2) // первым сообщением два актора связываются между собой
                         // NB: В первом сообщении мы отправляем эктора, с которым следует обмениваться.
        println("started Pinging")
        // изменим поведение главного актора. 
        // Теперь при получении сообщения он завершит работу всей системы.
        Behaviors.receive{ (_, _) =>  
          Behaviors.stopped // на этом работа корневого актора завершается
        }
      }
    }

  @Test def testSimple: Unit =
    val pingPongMain: ActorSystem[NotUsed] = ActorSystem(guard(), "guard")

    //#main-send-messages
    pingPongMain ! NotUsed  // первое сообщение начнёт процесс
    Thread.sleep(100) // подождём, пока всё произойдёт
    pingPongMain ! NotUsed  // вторым сообщением ликвидируем всю подсистему
    //#main-send-messages
    Await.result(pingPongMain.whenTerminated, 1.second) // ждём пока система самозавершится (без ошибок)
