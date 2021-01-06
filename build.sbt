val scala3Version = "3.0.0-M3"
val mainVersion = "0.1.0-SNAPSHOT"

ThisBuild / organization := "ru.primetalk"
ThisBuild / version      := mainVersion
ThisBuild / scalaVersion := scala3Version

val commonSettings = Seq(
  scalaVersion := scala3Version,
  libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.11" % "test",
  //    "org.scalatest" %% "scalatest" % "3.1.0" % Test
  )
)

lazy val root = (project in file("."))
  .aggregate(
    concurrency, 
    akkaExamples,
  )
  .settings(
    name := "scala-study"
  )

lazy val concurrency = project
  .in(file("concurrency"))
  .settings(
    name := "concurrency",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "2.3.1",
  )
  .settings(commonSettings :_*)

lazy val akkaVersion = "2.6.10"

lazy val akkaExamples = project
  .in(file("akkaExamples"))
  .settings(
    name := "akkaExamples",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.3.1",
      "com.typesafe.akka" % "akka-actor-typed_2.13" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.akka" % "akka-actor-testkit-typed_2.13" % akkaVersion % Test,
    ),
  )
  .settings(commonSettings :_*)
