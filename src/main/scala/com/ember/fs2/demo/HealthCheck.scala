package com.ember.fs2.demo

import cats.effect.IO
import cats.implicits._
import HealthCheck.IsNotRunningCheck
import org.http4s.dsl.io._
//import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.{HttpRoutes, Response, Status}

class HealthCheck(isNotRunning: IsNotRunningCheck) {
  def httpRoutes(): HttpRoutes[IO] =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "readiness" =>
          checkAndRespond()
      }
      .<+>(HttpRoutes.of[IO] {
        case GET -> Root / "liveness" =>

//          val  bufferMetric =
//            metricsSvc.collectorRegistry.getSampleValue(
//              "jvm_buffer_pool_used_bytes", Array("pool"), Array("direct")
//            )
//            System.out.println(s"11-jvm_buffer_pool_used_bytes: $bufferMetric")

          checkAndRespond()
      })

  private def checkAndRespond(): IO[Response[IO]] =
    Response[IO](if (isNotRunning()) Status.ServiceUnavailable else Status.Ok).pure[IO]
}

object HealthCheck {
  type IsNotRunningCheck = () => Boolean
}

