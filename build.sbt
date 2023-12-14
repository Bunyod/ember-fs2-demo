
ThisBuild / scalaVersion := "2.13.8"
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "ember-fs2-demo",
    Defaults.itSettings,
    IntegrationTest / testForkedParallel := false,
    scalacOptions ++= CompilerOptions.scalac213Options,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.5.2",
      "co.fs2" %% "fs2-core" % "3.9.3",
      "co.fs2" %% "fs2-io" % "3.9.3",
      "org.http4s" %% "http4s-dsl" % "0.23.24",
      "org.http4s" %% "http4s-ember-server" % "0.23.24",
      "org.http4s" %% "http4s-circe" % "0.23.24",
      "org.http4s" %% "http4s-prometheus-metrics" % "0.24.3",
      "org.scalatest" %% "scalatest" % "3.2.17" % "it,test"
    ),
    Compile / run / mainClass := Some("com.ember.fs2.demo.Main"),
  )
