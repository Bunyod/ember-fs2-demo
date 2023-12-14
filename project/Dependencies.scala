import sbt.*

object Dependencies {

  object Versions {
    val cats             = "2.9.0"
    val catsEffect       = "3.5.2"
    val betterMonadicFor = "0.3.1"
    val fs2              = "3.9.3"
    val kindProjector    = "0.13.2"
    val http4s           = "0.23.24"
  }

  object Libraries {
    val cats       = "org.typelevel"    %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel"    %% "cats-effect" % Versions.catsEffect

    def http4s(artifact: String): ModuleID = "org.http4s"   %% s"http4s-$artifact" % Versions.http4s

    val fs2Core = "co.fs2" %% "fs2-core" % Versions.fs2
    val fs2Io   = "co.fs2" %% "fs2-io"   % Versions.fs2

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sCirce  = http4s("circe")
  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
    val kindProjector    = compilerPlugin(
      ("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full)
    )
  }

  lazy val rootDependencies: Seq[ModuleID] =
    Seq(
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.fs2Core,
      Libraries.fs2Io,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      compilerPlugin(CompilerPlugins.kindProjector.cross(CrossVersion.full)),
      compilerPlugin(CompilerPlugins.betterMonadicFor),
      CompilerPlugins.kindProjector,
      "org.http4s" %% "http4s-prometheus-metrics" % "0.24.3",
      "org.scalatest" %% "scalatest" % "3.2.17" % "it,test"
    )

}
