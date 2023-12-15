package com.ember.fs2.demo.demo

import cats.effect.{ExitCode, IO}
import cats.effect.unsafe.implicits.global
import com.ember.fs2.demo.EmberMain
import fs2.concurrent.SignallingRef
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers._

import java.net.{HttpURLConnection, URL}
import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.io.Source

class EmberMainItSpec extends AnyFunSpec with BeforeAndAfterAll with BeforeAndAfterEach with Eventually {
  def logMemory(): Unit = {
    import java.lang.management.{BufferPoolMXBean, ManagementFactory}
    import scala.jdk.CollectionConverters._
    val pools: List[BufferPoolMXBean] = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.toList
    pools.foreach { pool =>
      System.out.println(String.format(
        "%s %d/%d",
        pool.getName,
        pool.getMemoryUsed,
        pool.getTotalCapacity));
    }
  }
  private lazy val signal = SignallingRef[IO, Boolean](false).unsafeRunSync()
  @volatile private var _mainObj = Option.empty[EmberMain]
  @volatile private var exit: Either[Throwable, ExitCode] = Right(ExitCode.Error)
  lazy val mainObj: EmberMain = {
    val x = new EmberMain()
    _mainObj = Some(x)
    x.run().unsafeRunAsync { x =>
      x.swap.foreach(_.printStackTrace())
      exit = x
    }
    ping() // ensure that server is up
    x
  }

  override protected def beforeEach(): Unit = {
    mainObj // initialize
    ()
  }

  override protected def afterAll(): Unit = {
    _mainObj.foreach { _ =>
      signal.set(true).unsafeRunSync()
      eventually(Timeout(10.seconds)) {
        exit must be(Right(ExitCode.Error))
      }
      ()
    }
  }

  private def getRequest(url: String): String = {
    eventually(Timeout(10.seconds)) {
      val connection = new URL(url)
        .openConnection().asInstanceOf[HttpURLConnection]
      val code = connection.getResponseCode
      code mustEqual 200
      Option(connection.getInputStream).map { inputStream =>
        val content = Source.fromInputStream(inputStream).mkString
        inputStream.close()
        content
      }.getOrElse("")
    }
  }

  private def ping(): Unit = {
    eventually(Timeout(10.seconds)) {
      getRequest(s"http://localhost:5000/ping")
    }
    ()
  }

  it("/ping") {
    ping()
  }

  it("/metrics") {
    val metrics = getRequest(s"http://localhost:5000/metrics")
    metrics must not be empty
  }

  it("show direct buffer leak") {
    val pool = Executors.newFixedThreadPool(10)
    val ec = ExecutionContext.fromExecutor(pool)
    implicit def context: ExecutionContextExecutor = ec
    val result = Future.sequence((0 until 10000).map { _ =>
      Future{
        getRequest(s"http://localhost:5000/ping")
        logMemory()
      }
    })
    Await.result(result, 20.seconds)
    pool.shutdown()
  }
}

