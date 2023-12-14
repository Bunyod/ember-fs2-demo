
ThisBuild / scalaVersion := "2.13.8"
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "ember-fs2-demo",
    Defaults.itSettings,
    IntegrationTest / testForkedParallel := false,
    scalacOptions ++= CompilerOptions.scalac213Options,
    libraryDependencies ++= Dependencies.rootDependencies,
    Compile / run / mainClass := Some("com.ember.fs2.demo.Main"),
  )
