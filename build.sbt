name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "4.9.0"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.10"
libraryDependencies += "org.jfree" % "jfreechart" % "1.5.0"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.2" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class", _*) => MergeStrategy.discard
  case x => (assemblyMergeStrategy in assembly).value(x)
}