package com.ember.fs2.demo

import cats.effect._
import cats.implicits._
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.middleware.Metrics

class Main() {
  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "ping" => Ok("pong") }

  def run(): IO[ExitCode] = {
    (
      for {
        metricsSvc <- PrometheusExportService.build[IO]
        metrics <- Prometheus.metricsOps[IO](metricsSvc.collectorRegistry, "demo_app_public")
        httpApp = Metrics[IO](metrics)(routes <+> metricsSvc.routes).orNotFound
        server <- EmberServerBuilder
          .default[IO]
          .withHost(host"0.0.0.0")
          .withPort(port"5000")
          .withHttpApp(httpApp)
          .build
      } yield server
    ).useForever
  }

}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    new Main().run()
  }
}
