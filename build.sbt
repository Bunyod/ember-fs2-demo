ThisBuild / scalaVersion := "2.13.8"
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `ember-demo` = (project in file("ember-demo"))
  .settings(
    name := "ember-demo",
    Defaults.itSettings,
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
    )
  )

lazy val `blaze-demo` = (project in file("blaze-demo"))
  .settings(
    name := "blaze-demo",
    scalaVersion := "2.13.8",
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.5.2",
      "co.fs2" %% "fs2-core" % "3.9.3",
      "co.fs2" %% "fs2-io" % "3.9.3",
      "org.http4s" %% "http4s-prometheus-metrics" % "0.24.3",
      "org.scalatest" %% "scalatest" % "3.2.17" % "it,test",
      "org.http4s" %% "http4s-blaze-server" % "0.23.15",
      "org.http4s" %% "http4s-core" % "0.23.15",
      "org.http4s" %% "http4s-dsl" % "0.23.15",
      "org.http4s" %% "http4s-circe" % "0.23.15",
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := "ember-fs2-demo",
    scalacOptions ++= CompilerOptions.cOptions
  ).aggregate(`ember-demo`, `blaze-demo`)
