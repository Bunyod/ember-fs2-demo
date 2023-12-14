package com.ember.fs2.demo

import cats.effect._
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.slf4j.LoggerFactory

class Main(config: HttpConfig) {
  private val adminConfig = HttpConfig("0.0.0.0", 5004)
  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(): IO[ExitCode] = {
    logger.info("Starting test")
    (
      for {
        metricsSvc <- PrometheusExportService.build[IO]
        metrics <- Prometheus.metricsOps[IO](metricsSvc.collectorRegistry, "demo_app_public")
        adminServer <- MkHttpServer[IO].newEmber(
          adminConfig,
          new AdminHttpServer(metricsSvc).httpApp(() => false)
        )
        publicServer <- MkHttpServer[IO].newEmber(config, new PublicHttpServer(metrics).httpApp)
      } yield (adminServer, publicServer)
      ).useForever
  }
}

object Main extends IOApp {

  java.security.Security.setProperty("networkaddress.cache.ttl", "60")

  private val config = HttpConfig("0.0.0.0", 5000)
  private val mainObj = new Main(config)

  override def run(args: List[String]): IO[ExitCode] = {
    mainObj.run()
  }
}
