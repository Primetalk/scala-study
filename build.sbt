val scala3Version = "3.0.0-M3"
val mainVersion = "0.1.0-SNAPSHOT"

ThisBuild / organization := "ru.primetalk"
ThisBuild / version      := mainVersion
ThisBuild / scalaVersion := scala3Version

val commonSettings = Seq(
  scalaVersion := scala3Version,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
)
lazy val root = (project in file("."))
  .aggregate(example)
  .settings(
    name := "scala-study"
  )

lazy val example = project
  .in(file("example"))
  .settings(
    name := "example",
  )
  .settings(commonSettings :_*)
