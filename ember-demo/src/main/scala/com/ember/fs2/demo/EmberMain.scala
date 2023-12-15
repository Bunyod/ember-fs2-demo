package com.ember.fs2.demo

import cats.effect._
import cats.implicits._
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.middleware.Metrics

class EmberMain(port: Int) {
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
        server <- EmberServerBuilder
          .default[IO]
          .withHost(host"0.0.0.0")
          .withPort(Port.fromInt(port).getOrElse(port"5000"))
          .withHttpApp(httpApp)
          .build
      } yield server
    ).useForever
  }

  def logMemory(): Unit = {
    import java.lang.management.{BufferPoolMXBean, ManagementFactory}
    import scala.collection.JavaConverters._
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

object EmberMain extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    new EmberMain(5000).run()
  }
}
