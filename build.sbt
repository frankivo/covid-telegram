//import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "covid-telegram"
  )

ThisBuild / libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "5.5.0"
ThisBuild / libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.9"
ThisBuild / libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.18"
ThisBuild / libraryDependencies += "org.jfree" % "jfreechart" % "1.5.3"
ThisBuild / libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.3.18"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.10" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")
//
//scalacOptions := Seq("-unchecked", "-deprecation")
//
//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)
//
//dockerBaseImage := "openjdk:8-alpine"
//dockerAlias := dockerAlias.value.withName("oosterhuisf/covid-telegram").withTag(Option("latest"))
//
//dockerCommands := dockerCommands.value.flatMap {
//  case Cmd("USER", args@_*) if args.contains("1001:0") => Seq(
//    Cmd("RUN", "apk add --no-cache ttf-dejavu bash"),
//    Cmd("USER", args: _*)
//  )
//  case cmd => Seq(cmd)
//}
