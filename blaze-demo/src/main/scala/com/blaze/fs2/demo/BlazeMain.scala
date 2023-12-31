package com.blaze.fs2.demo

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.middleware.Metrics

class BlazeMain(port: Int) {
  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "ping" =>
    logMemory()
    Ok("pong")
  }

  def run(): IO[ExitCode] = {
    (
      for {
        metricsSvc <- PrometheusExportService.build[IO]
        metrics <- Prometheus.metricsOps[IO](metricsSvc.collectorRegistry, "demo_app_public")
        httpApp = Metrics[IO](metrics)(routes <+> metricsSvc.routes).orNotFound
        server <- Resource.eval(BlazeServerBuilder[IO]
          .bindHttp(port = port, host = "0.0.0.0")
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
        )
      } yield server
    ).useForever
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

object BlazeMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    new BlazeMain(6000).run()
  }
}
