import com.typesafe.sbt.packager.docker.Cmd

name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "5.1.0"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.8"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.14"
libraryDependencies += "org.jfree" % "jfreechart" % "1.5.3"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.10" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerBaseImage := "openjdk:8-alpine"
dockerAlias := dockerAlias.value.withName("oosterhuisf/covid-telegram").withTag(Option("latest"))

dockerCommands := dockerCommands.value.filterNot {
  case Cmd("USER", _*) => true
  case _ => false
} :+ Cmd("RUN", "apk add --no-cache ttf-dejavu bash")
