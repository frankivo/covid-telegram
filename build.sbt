name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.3"

lazy val exclude = Seq(
  ExclusionRule(organization = "com.fasterxml.jackson.core"),
  ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
  ExclusionRule(organization = "com.google.code.gson")
)

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.11.3"
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.6"

libraryDependencies += ("com.github.pengrad" % "java-telegram-bot-api" % "4.9.0" excludeAll (exclude: _*))
libraryDependencies += ("com.typesafe.akka" %% "akka-actor" % "2.6.10" excludeAll (exclude: _*))
libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.9.1" excludeAll (exclude: _*))
libraryDependencies += ("org.scalaj" %% "scalaj-http" % "2.4.2" excludeAll (exclude: _*))

libraryDependencies += ("com.lihaoyi" %% "utest" % "0.7.2" % "test" excludeAll (exclude: _*))
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")
