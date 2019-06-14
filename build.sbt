import BuildProperties._
import Dependencies._
import sbt._

lazy val IT = config("it") extend Test

lazy val root = project("akka-backwards", file("."))
  .settings(description := "Backwards Akka module aggregation - Akka functionality includes example usage in various courses")
  .aggregate(persistenceCourse)

lazy val persistenceCourse = project("persistence", file("courses/persistence"))
  .settings(description := "Persistence Course")
  .settings(javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf"))

// TODO - Somehow reuse from module "scala-backwards"
def project(id: String, base: File): Project =
  Project(id, base)
    .configs(IT)
    .settings(inConfig(IT)(Defaults.testSettings))
    .settings(Defaults.itSettings)
    .settings(
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayRepo("cakesolutions", "maven"),
        "jitpack" at "https://jitpack.io",
        "Confluent Platform Maven" at "http://packages.confluent.io/maven/"
      ),
      scalaVersion := BuildProperties("scala.version"),
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards",
      name := id,
      autoStartServer := false,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      libraryDependencies ++= dependencies,
      excludeDependencies ++= Seq("org.slf4j" % "slf4j-log4j12"),
      fork := true,
      javaOptions in IT ++= environment.map { case (key, value) => s"-D$key=$value" }.toSeq,
      scalacOptions ++= Seq("-Ypartial-unification"),
      assemblyJarName in assembly := s"$id.jar",
      test in assembly := {},
      assemblyMergeStrategy in assembly := {
        case PathList("javax", xs @ _*) => MergeStrategy.first
        case PathList("org", xs @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
        case "application.conf" => MergeStrategy.concat
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      }
    )