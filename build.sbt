name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "4.9.0"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.10"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.32.3.2"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.2" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")