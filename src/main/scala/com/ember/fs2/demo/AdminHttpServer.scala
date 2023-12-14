package com.ember.fs2.demo

import cats.effect.IO
import cats.implicits._
import org.http4s.implicits._
import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.{HttpApp, HttpRoutes}

class AdminHttpServer(metricsSvc: PrometheusExportService[IO]) {

  private def serviceHealth(isNotRunningCheck: HealthCheck.IsNotRunningCheck): HttpRoutes[IO] =
    new HealthCheck(isNotRunningCheck).httpRoutes()

  def httpApp(isNotRunningCheck: HealthCheck.IsNotRunningCheck): HttpApp[IO] =
    (metricsSvc.routes <+> serviceHealth(isNotRunningCheck)).orNotFound

}