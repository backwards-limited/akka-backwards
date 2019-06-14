import sbt._

object Dependencies {
  lazy val dependencies: Seq[ModuleID] =
    Seq(
      backwards,
      scalatest, testcontainers, scalatestContainers, airframe, logging, pprint, pureConfig, betterFiles, apacheCommons,
      cats, monocle, shapeless, fs2, http4s, sttp, scalaUri, akka,
      avro4s, protobuf, circe,
      leveldb, postgresql
    ).flatten

  lazy val backwards: Seq[ModuleID] = {
    val version = "1.0.25"

    Seq(
      "com.github.backwards-limited" % "scala-backwards" % version
    ) ++ Seq(
      "com.github.backwards-limited" % "scala-backwards" % version % "test, it" classifier "tests",
      "com.github.backwards-limited" % "scala-backwards" % version % "test, it" classifier "it"
    )
  }

  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % "test, it"
  )

  lazy val testcontainers: Seq[ModuleID] = Seq(
    "org.testcontainers" % "testcontainers" % "1.11.3" % "test, it"
  )
  
  lazy val scalatestContainers: Seq[ModuleID] = Seq(
    "com.dimafeng" %% "testcontainers-scala" % "0.26.0" % "test, it"
  )

  lazy val airframe: Seq[ModuleID] = Seq(
    "org.wvlet.airframe" %% "airframe-log" % "19.6.1"
  )

  lazy val logging: Seq[ModuleID] = Seq(
    "org.slf4j" % "log4j-over-slf4j" % "1.7.26",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  lazy val pprint: Seq[ModuleID] = Seq(
    "com.lihaoyi" %% "pprint" % "0.5.5" % "test, it"
  )

  lazy val pureConfig: Seq[ModuleID] = {
    val version = "0.11.0"

    Seq(
      "com.github.pureconfig" %% "pureconfig",
      "com.github.pureconfig" %% "pureconfig-http4s"
    ).map(_ % version)
  }

  lazy val betterFiles: Seq[ModuleID] = Seq(
    "com.github.pathikrit" %% "better-files" % "3.8.0"
  )

  lazy val apacheCommons: Seq[ModuleID] = Seq(
    "org.apache.commons" % "commons-lang3" % "3.9"
  )

  lazy val cats: Seq[ModuleID] = {
    val version = "1.6.1"

    Seq(
      "org.typelevel" %% "cats-effect" % "1.3.1"
    ) ++ Seq(
      "org.typelevel" %% "cats-core"
    ).map(_ % version) ++ Seq(
      "org.typelevel" %% "cats-laws",
      "org.typelevel" %% "cats-testkit"
    ).map(_ % version % "test, it")
  }
  
  lazy val monocle: Seq[ModuleID] = {
    val version = "1.5.0"

    Seq(
      "com.github.julien-truffaut" %% "monocle-core",
      "com.github.julien-truffaut" %% "monocle-macro",
      "com.github.julien-truffaut" %% "monocle-generic"
    ).map(_ % version) ++ Seq(
      "com.github.julien-truffaut" %% "monocle-law"
    ).map(_ % version % "test, it")
  }

  lazy val shapeless: Seq[ModuleID] = Seq(
    "com.chuusai" %% "shapeless" % "2.3.3"
  )
  
  lazy val fs2: Seq[ModuleID] = {
    val version = "1.0.5"
    
    Seq(
      "co.fs2" %% "fs2-core",
      "co.fs2" %% "fs2-io",
      "co.fs2" %% "fs2-reactive-streams"
    ).map(_ % version)
  }

  lazy val http4s: Seq[ModuleID] = {
    val version = "0.20.3"

    Seq(
      "org.http4s" %% "http4s-testing",
      "org.http4s" %% "http4s-dsl"
    ).map(_ % version % "test, it") ++ Seq(
      "org.http4s" %% "http4s-core",
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-client",
      "org.http4s" %% "http4s-circe"
    ).map(_ % version)
  }

  lazy val sttp: Seq[ModuleID] = {
    val version = "1.5.19"

    Seq(
      "com.softwaremill.sttp" %% "core",
      "com.softwaremill.sttp" %% "circe"
    ).map(_ % version)
  }

  lazy val scalaUri: Seq[ModuleID] = Seq(
    "io.lemonlabs" %% "scala-uri" % "1.4.5"
  )

  lazy val akka: Seq[ModuleID] = {
    val version = "2.5.23"

    Seq(
      "com.typesafe.akka" %% "akka-actor",
      "com.typesafe.akka" %% "akka-stream",
      "com.typesafe.akka" %% "akka-cluster",
      "com.typesafe.akka" %% "akka-cluster-tools",
      "com.typesafe.akka" %% "akka-cluster-sharding",
      "com.typesafe.akka" %% "akka-remote",
      "com.typesafe.akka" %% "akka-distributed-data",
      "com.typesafe.akka" %% "akka-persistence",
      "com.typesafe.akka" %% "akka-persistence-query",
      "com.typesafe.akka" %% "akka-persistence-tck"
    ).map(_ % version) ++ Seq(
      "com.typesafe.akka" %% "akka-testkit",
      "com.typesafe.akka" %% "akka-stream-testkit"
    ).map(_ % version % "test, it")
  } ++ {
    val version = "0.98"

    Seq(
      "com.typesafe.akka" %% "akka-persistence-cassandra" % version
    ) ++ Seq(
      "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % version % "test, it"
    )
  } ++ Seq(
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.1"
  )

  lazy val avro4s: Seq[ModuleID] = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.4"
  )
  
  lazy val protobuf: Seq[ModuleID] = Seq(
    "com.google.protobuf" % "protobuf-java" % "3.8.0"
  )

  lazy val circe: Seq[ModuleID] = {
    val version = "0.11.1"

    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-generic-extras",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-refined"
    ).map(_ % version) ++ Seq(
      "io.circe" %% "circe-testing",
      "io.circe" %% "circe-literal"
    ).map(_ % version % "test, it")
  }

  lazy val leveldb: Seq[ModuleID] = Seq(
    "org.iq80.leveldb" % "leveldb" % "0.11",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )
  
  lazy val postgresql: Seq[ModuleID] = Seq(
    "org.postgresql" % "postgresql" % "42.2.5"
  )
}