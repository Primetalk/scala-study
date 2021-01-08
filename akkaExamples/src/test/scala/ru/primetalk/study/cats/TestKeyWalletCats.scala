package ru.primetalk.study.cats

import cats.effect.{FiberIO, IO, OutcomeIO}
import cats.effect.Deferred
import org.junit.Test

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

class TestKeyWalletCats:
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def threadId: Long = Thread.currentThread().getId

  case class Keys()
  case class Wallet()

  object KeyCabinet:
    def obtainKeys(whoseKeys: String): IO[Keys] = IO {
      println(s"obtaining Keys ($threadId)")
      Thread.sleep(500)
      Keys()
    }

  object Drawer:
    def obtainWallet(whoseWallet: String): IO[Wallet] = IO {
      println(s"obtaining Wallet ($threadId)")
      Thread.sleep(500)
      Wallet()
    }
  
  case class ReadyToLeaveHome(who: String, keys: Keys, wallet: Wallet)

  @Test def testKeyWalletSingle: Unit =
    def leaveHome(who: String): IO[ReadyToLeaveHome] =
      val keysIO = KeyCabinet.obtainKeys(who)
      val walletIO = Drawer.obtainWallet(who)
      for
        keys <- keysIO
        wallet <- walletIO
      yield ReadyToLeaveHome(who, keys, wallet)
    
    assert(leaveHome("Bobby").unsafeRunSync() ==
      ReadyToLeaveHome("Bobby", Keys(), Wallet()))

  @Test def testKeyWalletFiber: Unit =
    def leaveHomeFiber(who: String): IO[ReadyToLeaveHome] =
      val keysIO = KeyCabinet.obtainKeys(who)
      val walletIO = Drawer.obtainWallet(who)
      println(s"leaveHomeFiber ($threadId)")
      for
        keysFiber <- keysIO.start
        walletFiber <- walletIO.start
        keysResult <- keysFiber.join
        walletResult <- walletFiber.join
        keys <- keysResult.embedNever
        wallet <- walletResult.embedNever
        _ <- IO{ println(s"obtained both keys and wallet ($threadId)") } 
      yield ReadyToLeaveHome(who, keys, wallet)

    assert(leaveHomeFiber("Bobby").unsafeRunSync() ==
      ReadyToLeaveHome("Bobby", Keys(), Wallet()))

  @Test def testKeyWalletDeferred: Unit =
    extension[A](io: IO[A])
      def >>>(result: Deferred[IO, A]): IO[Unit] = io.flatMap(result.complete).map(_ => ())

    def startIOinFiber[A](io: IO[A], result: Deferred[IO, A]): IO[FiberIO[Unit]] =
       (io >>> result).start 

    def leaveHomeWithDeferred(who: String): IO[ReadyToLeaveHome] =
      val keysIO = KeyCabinet.obtainKeys(who)
      val walletIO = Drawer.obtainWallet(who)
      println(s"leaveHomeWithDeferred ($threadId)")
      for 
        deferredKeys <- Deferred[IO, Keys]
        deferredWallet <- Deferred[IO, Wallet]
        _ <- startIOinFiber(keysIO, deferredKeys)
        _ <- startIOinFiber(walletIO, deferredWallet)
        keys <- deferredKeys.get
        wallet <- deferredWallet.get
        _ <- IO{ println(s"obtained both keys and wallet ($threadId)") }
      yield ReadyToLeaveHome(who, keys, wallet)

    assert(leaveHomeWithDeferred("Bobby").unsafeRunSync() == 
      ReadyToLeaveHome("Bobby", Keys(), Wallet()))
