package com.ember.fs2.demo

import cats.effect.kernel.{Async, Resource}
import cats.implicits._
import com.comcast.ip4s.{IpAddress, Port}
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.slf4j.{Logger, LoggerFactory}

trait MkHttpServer[F[_]] {
  def newEmber(cfg: HttpConfig, httpApp: HttpApp[F]): Resource[F, Server]
}

object MkHttpServer {
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = implicitly
  private val logger: Logger                     = LoggerFactory.getLogger(getClass)
  private def showEmberBanner[F[_]: Async](s: Server): F[Unit] =
    logger.info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}").pure[F]

  implicit def forAsyncLogger[F[_]: Async: Network]: MkHttpServer[F] =
    (cfg: HttpConfig, httpApp: HttpApp[F]) =>
      (
        IpAddress.fromString(cfg.host),
        Port.fromInt(cfg.port)
      ).mapN { (host, port) =>
          EmberServerBuilder
            .default[F]
            .withHost(host)
            .withPort(port)
            .withHttpApp(httpApp)
            .build
            .evalTap(showEmberBanner[F](_))
        }
        .getOrElse(Resource.raiseError(new Throwable("Couldn't load server configurations")))
}
