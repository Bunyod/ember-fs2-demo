package com.ember.fs2.demo.demo

import cats.effect.{ExitCode, IO}
import cats.effect.unsafe.implicits.global
import com.ember.fs2.demo.HttpConfig
import com.ember.fs2.demo.Main
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

class MainItSpec extends AnyFunSpec with BeforeAndAfterAll with BeforeAndAfterEach with Eventually {
  private lazy val config = HttpConfig(host = "0.0.0.0", port = 9009)
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
  @volatile private var _mainObj = Option.empty[Main]
  @volatile private var exit: Either[Throwable, ExitCode] = Right(ExitCode.Error)
  lazy val mainObj: Main = {
    val x = new Main(config)
    _mainObj = Some(x)
    x.run().unsafeRunAsync { x =>
      x.swap.foreach(_.printStackTrace())
      exit = x
    }
    liveness() // ensure that server is up
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

  private def liveness(): Unit = {
    eventually(Timeout(10.seconds)) {
      getRequest(s"http://localhost:5004/liveness")
    }
    ()
  }

  it("/liveness") {
    liveness()
  }

  it("/metrics") {
    val metrics = getRequest(s"http://localhost:5004/metrics")
    metrics must not be empty
  }

  it("show direct buffer leak") {
    val pool = Executors.newFixedThreadPool(10)
    val ec = ExecutionContext.fromExecutor(pool)
    implicit def context: ExecutionContextExecutor = ec
    val result = Future.sequence((0 until 10000).map { _ =>
      Future{
        getRequest(s"http://localhost:5004/liveness")
        logMemory()
      }
    })
    Await.result(result, 20.seconds)
    pool.shutdown()

  }
}

