package com.ember.fs2.demo

import cats.effect._
import org.http4s._
import org.http4s.metrics.MetricsOps
import org.http4s.server.middleware._

class PublicHttpServer(
    metrics: MetricsOps[IO]
) {

  val httpApp: HttpApp[IO]  = {
    val routes: HttpRoutes[IO] = ExampleService.apply[IO].routes

    Metrics[IO](metrics)(routes).orNotFound
  }

}
