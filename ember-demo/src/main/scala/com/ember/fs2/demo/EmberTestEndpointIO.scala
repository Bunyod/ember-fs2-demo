package com.ember.fs2.demo

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.ember.client.EmberClientBuilder

object EmberTestEndpointIO extends IOApp {

  def run: IO[List[String]] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      (0 until 10000).toList.parTraverse(_ =>
        client
          .expect[String]("http://localhost:5000/ping")
      )
    }

  override def run(args: List[String]): IO[ExitCode] = {
    println("starting ember server test")

    run.map { _ =>
      println("finished")
      ExitCode.Success
    }
  }

}
