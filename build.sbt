val scala3Version = "3.0.0-M3"
val mainVersion = "0.2.0-SNAPSHOT"

ThisBuild / organization := "ru.primetalk"
ThisBuild / version      := mainVersion
ThisBuild / scalaVersion := scala3Version

val catsEffect = "org.typelevel" % "cats-effect_3.0.0-M3" % "3.0.0-RC1"
val fs2 = libraryDependencies ++= Seq(
  "co.fs2" % "fs2-core_3.0.0-M3" % "3.0.0-M8",
  "co.fs2" % "fs2-io_3.0.0-M3" % "3.0.0-M8",
)
val commonSettings = Seq(
  scalaVersion := scala3Version,
  libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.11" % "test",
    catsEffect,
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
  )
  .settings(commonSettings :_*)

lazy val akkaVersion = "2.6.10"

lazy val akkaExamples = project
  .in(file("akkaExamples"))
  .settings(
    name := "akkaExamples",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-actor-typed_2.13" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.akka" % "akka-actor-testkit-typed_2.13" % akkaVersion % Test,
    ),
  )
  .settings(commonSettings :_*)

lazy val fs2Streaming = project
  .in(file("fs2Streaming"))
  .settings(
    name := "fs2Streaming",
    fs2,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-actor-typed_2.13" % akkaVersion,
      "com.typesafe.akka" % "akka-stream_2.13" % akkaVersion,
      "ru.primetalk" % "synapse-grid-core_2.13" % "1.5.0",
    ),
  )
  .settings(commonSettings:_*)

val Http4sVersion = "0.22.0-M3"//"0.21.19"
val CirceVersion = "0.13.0"
lazy val http4sTodoList = project
  .in(file("http4sTodoList"))
  .settings(
    name := "http4sTodoList",
    scalaVersion := scala3Version,
    libraryDependencies += "org.http4s" % "http4s-circe_2.13" % Http4sVersion,
    libraryDependencies += "org.http4s" % "http4s-core_2.13" % Http4sVersion,
    libraryDependencies += "org.http4s" % "http4s-dsl_2.13" % Http4sVersion,
    libraryDependencies += "org.http4s" % "http4s-circe_2.13" % Http4sVersion,
    libraryDependencies ++= Seq(
      "org.http4s"      % "http4s-blaze-server_2.13" % Http4sVersion,
      "org.http4s"      % "http4s-blaze-client_2.13" % Http4sVersion,
    ),

    libraryDependencies += "org.http4s" % "blaze-http_2.13" % "0.14.15",

    libraryDependencies += "io.circe" % "circe-core_2.13" % CirceVersion,
    libraryDependencies += "io.circe" % "circe-generic_2.13" % CirceVersion,
    libraryDependencies += "io.circe" % "circe-parser_2.13" % CirceVersion,
    libraryDependencies += "io.circe" % "circe-literal_2.13" % CirceVersion,
    mainClass := Some("ru.primetalk.study.rest.http4sexamples.TodoServer"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  )
