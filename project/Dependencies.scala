import sbt.*

object Dependencies {

  object Versions {
    val cats             = "2.9.0"
    val catsEffect       = "3.4.8"
    val logback          = "1.4.7"
    val betterMonadicFor = "0.3.1"
    val fs2              = "3.9.3"
    val kindProjector    = "0.13.2"
    val http4s           = "0.23.22"
    val circe            = "0.14.5"
    val catsRetry        = "3.1.0"
    val pureConfig       = "0.17.4"
  }

  object Libraries {
    val cats       = "org.typelevel"    %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel"    %% "cats-effect" % Versions.catsEffect
    val catsRetry  = "com.github.cb372" %% "cats-retry"  % Versions.catsRetry

    def http4s(artifact: String): ModuleID = "org.http4s"   %% s"http4s-$artifact" % Versions.http4s
    def circe(artifact: String): ModuleID  = "io.circe"     %% artifact            % Versions.circe

    val fs2Core = "co.fs2" %% "fs2-core" % Versions.fs2
    val fs2Io   = "co.fs2" %% "fs2-io"   % Versions.fs2

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val http4sCirce  = http4s("circe")
  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
    val kindProjector    = compilerPlugin(
      ("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full)
    )
  }

  val common: Seq[ModuleID] = Seq(
    compilerPlugin(CompilerPlugins.kindProjector.cross(CrossVersion.full)),
    compilerPlugin(CompilerPlugins.betterMonadicFor),
    CompilerPlugins.kindProjector
  )

  val cats: Seq[ModuleID] = Seq(
    Libraries.cats,
    Libraries.catsEffect,
    Libraries.catsRetry
  )

  val circe: Seq[ModuleID] = Seq(
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.circeRefined
  )

  val http4s: Seq[ModuleID] = Seq(
    Libraries.http4sDsl,
    Libraries.http4sClient,
    Libraries.http4sServer,
    Libraries.http4sCirce
  )

  lazy val rootDependencies: Seq[ModuleID] =
    Seq(
      Libraries.fs2Core,
      Libraries.fs2Io,
      "org.http4s" %% "http4s-prometheus-metrics" % "0.24.3",
      "org.scalatest" %% "scalatest" % "3.2.17" % "it,test",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "io.prometheus" % "simpleclient_logback" % "0.16.0"
    ) ++ common ++ cats ++ circe ++ http4s

}
