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
  println("starting blaze server test")
  val result = Future.sequence((0 until 1000000).map { _ =>
    Future {
      getRequest(s"http://localhost:6000/ping")
    }
  })
  Await.result(result, 60.seconds)
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
}
