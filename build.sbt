val scala3Version = "3.0.0"
val mainVersion = "0.5.0-SNAPSHOT"

ThisBuild / organization := "ru.primetalk"
ThisBuild / version      := mainVersion
ThisBuild / scalaVersion := scala3Version

val catsEffect = "org.typelevel" %% "cats-effect" % "3.1.1"
val fs2 = libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "3.0.4",
  "co.fs2" %% "fs2-io" % "3.0.4",
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
    fs2Streaming,
    http4sTodoList,
    akkaHttpTodoList,
  )
  .settings(
    name := "scala-study"
  )

lazy val dataStructures = project
  .in(file("dataStructures"))
  .settings(
    name := "dataStructures",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4"
  )
  .settings(commonSettings :_*)

lazy val concurrency = project
  .in(file("concurrency"))
  .settings(
    name := "concurrency",
  )
  .settings(commonSettings :_*)

lazy val akkaVersion    = "2.6.14"
lazy val akkaHttpVersion = "10.2.4"

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

//libraryDependencies += "org.http4s" % "http4s-core_3.0.0-RC2" % "1.0.0-M21"

val Http4sVersion = "1.0.0-M22"//"0.21.19"
//val Http4sVersion = "0.22.0-M3"//"0.21.19"
val CirceVersion = "0.14.0-M7"//"0.13.0"
val circe = Seq(
  "io.circe" %% "circe-core" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
//  "io.circe" %% "circe-literal" % CirceVersion,
)

lazy val http4sTodoList = project
  .in(file("http4sTodoList"))
  .settings(
    name := "http4sTodoList",
    scalaVersion := scala3Version,

    libraryDependencies += "org.http4s" %% "http4s-core" % Http4sVersion,
    libraryDependencies += "org.http4s" %% "http4s-dsl" % Http4sVersion,
    libraryDependencies += "org.http4s" %% "http4s-circe" % Http4sVersion,
    libraryDependencies += "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    libraryDependencies += "org.http4s" %% "http4s-blaze-client" % Http4sVersion,


    libraryDependencies ++= circe,
    mainClass := Some("ru.primetalk.study.rest.http4sexamples.TodoServer"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  )

lazy val akkaHttpTodoList = project
  .in(file("akkaHttpTodoList"))
  .settings(
    name := "akkaHttpTodoList",
    scalaVersion := scala3Version,

    libraryDependencies ++= circe,
    mainClass := Some("ru.primetalk.study.rest.akkahttpexamples.TodoServer"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-http_2.13"                % akkaHttpVersion,
      "com.typesafe.akka" % "akka-http-spray-json_2.13"     % akkaHttpVersion,
      "com.typesafe.akka" % "akka-actor-typed_2.13"         % akkaVersion,
      "com.typesafe.akka" % "akka-stream_2.13"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",

      "com.typesafe.akka" % "akka-http-testkit_2.13"        % akkaHttpVersion % Test,
      "com.typesafe.akka" % "akka-actor-testkit-typed_2.13" % akkaVersion     % Test,
      "org.scalatest"     % "scalatest_2.13"                % "3.1.4"         % Test
    )

  )
