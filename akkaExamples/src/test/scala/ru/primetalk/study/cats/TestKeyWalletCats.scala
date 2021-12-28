package ru.primetalk.study.cats

import cats.effect.{FiberIO, IO, OutcomeIO}
import cats.effect.Deferred
import org.junit.Test

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

class TestKeyWalletCats:
  // Вспомогательная функция, позволяющая узнать идентификатор текущего потока
  def threadId: Long = Thread.currentThread().getId

  case class Keys()
  case class Wallet()

  object KeyCabinet:
    def obtainKeys(whoseKeys: String): IO[Keys] = IO {
      println(s"obtaining $whoseKeys's Keys ($threadId)")
      Thread.sleep(500)
      Keys()
    }

  object Drawer:
    def obtainWallet(whoseWallet: String): IO[Wallet] = IO {
      println(s"obtaining $whoseWallet's Wallet ($threadId)")
      Thread.sleep(500)
      Wallet()
    }
  // Сообщение о готовности может быть создано только тогда, когда
  // в наличии есть ключи и бумажник.
  case class ReadyToLeaveHome(who: String, keys: Keys, wallet: Wallet)

  def reportReadiness(who: String, keys: Keys, wallet: Wallet): IO[ReadyToLeaveHome] = IO{ 
    println(s"obtained both keys and wallet for $who ($threadId)"); 
    ReadyToLeaveHome(who, keys, wallet) 
  }

  def leaveHomeSingle(who: String): IO[ReadyToLeaveHome] =
    val keysIO = KeyCabinet.obtainKeys(who)
    val walletIO = Drawer.obtainWallet(who)
    println(s"$who is leaving home ($threadId)")
    for
      keys <- keysIO
      wallet <- walletIO
      ready <- reportReadiness(who, keys, wallet)
    yield
      ready

  @Test def testKeyWalletSingleThread: Unit =
    assert(leaveHomeSingle("BobbySingleThread").unsafeRunSync() ==
      ReadyToLeaveHome("BobbySingleThread", Keys(), Wallet()))

  // используем легковесные потоки/fiber/"ниточки"
  def leaveHomeFiber(who: String): IO[ReadyToLeaveHome] =
    val keysIO = KeyCabinet.obtainKeys(who)
    val walletIO = Drawer.obtainWallet(who)
    println(s"$who is leaving home ($threadId)")
    for
      keysFiber <- keysIO.start // запуск отдельной легковесной нити (ниточки, fiber)
      walletFiber <- walletIO.start
      keysResult <- keysFiber.join
      walletResult <- walletFiber.join
      keys <- keysResult.embedNever
      wallet <- walletResult.embedNever
      ready <- reportReadiness(who, keys, wallet)
    yield
      ready

  @Test def testKeyWalletFiber: Unit =
    assert(leaveHomeFiber("BobbyFiber").unsafeRunSync() ==
      ReadyToLeaveHome("BobbyFiber", Keys(), Wallet()))

  extension[A](io: IO[A])
    /** Сохранить результат в указанную синхронизированную переменную. */
    def >>>(result: Deferred[IO, A]): IO[Unit] = io.flatMap(result.complete).map(_ => ())

  def startIOinFiber[A](io: IO[A], result: Deferred[IO, A]): IO[FiberIO[Unit]] =
      (io >>> result).start 

  def leaveHomeWithDeferred(who: String): IO[ReadyToLeaveHome] =
    val keysIO = KeyCabinet.obtainKeys(who)
    val walletIO = Drawer.obtainWallet(who)
    println(s"$who is leaving home ($threadId)")
    for 
      deferredKeys <- Deferred[IO, Keys]
      deferredWallet <- Deferred[IO, Wallet]
      _ <- startIOinFiber(keysIO, deferredKeys)
      _ <- startIOinFiber(walletIO, deferredWallet)
      keys <- deferredKeys.get
      wallet <- deferredWallet.get
      ready <- reportReadiness(who, keys, wallet)
    yield
      ready

  @Test def testKeyWalletDeferred: Unit =
    assert(leaveHomeWithDeferred("BobbyDeferred").unsafeRunSync() == 
      ReadyToLeaveHome("BobbyDeferred", Keys(), Wallet()))
