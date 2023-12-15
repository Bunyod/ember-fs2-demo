package com.blaze.fs2.demo

import java.net.{HttpURLConnection, URL}
import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.Source

object BlazeTestEndpoint extends App {

  val pool = Executors.newFixedThreadPool(10)
  val ec = ExecutionContext.fromExecutor(pool)

  implicit def context: ExecutionContextExecutor = ec

  val result = Future.sequence((0 until 100000).map { _ =>
    Future {
      getRequest(s"http://localhost:6000/ping")
      logMemory()
    }
  })
  Await.result(result, 20.seconds)
  pool.shutdown()
  println("finished")
  System.exit(1)

  private def getRequest(url: String): String = {
      val connection = new URL(url)
        .openConnection().asInstanceOf[HttpURLConnection]
      Option(connection.getInputStream).map { inputStream =>
        val content = Source.fromInputStream(inputStream).mkString
        inputStream.close()
        content
      }.getOrElse("")
  }

  def logMemory(): Unit = {
    import java.lang.management.{BufferPoolMXBean, ManagementFactory}
    import scala.jdk.CollectionConverters._
    val pools: List[BufferPoolMXBean] = ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.toList
    pools.foreach { pool =>
      println(
        "%s %d/%d".format(
          pool.getName,
          pool.getMemoryUsed.doubleValue().toLong,
          pool.getTotalCapacity.doubleValue().toLong
        )
      )
    }
  }
}
