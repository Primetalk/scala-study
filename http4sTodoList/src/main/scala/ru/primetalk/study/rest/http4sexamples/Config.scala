package ru.primetalk.study.rest.http4sexamples

trait AbstractConfig:
  val port: Int
  val baseUrl: String

trait Config extends AbstractConfig:
  val port: Int = 8040
  val baseUrl: String = s"http://localhost:$port"
