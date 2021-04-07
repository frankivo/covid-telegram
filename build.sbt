name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "5.0.1"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.12"
libraryDependencies += "org.jfree" % "jfreechart" % "1.5.3"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.7" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")

assembly / assemblyMergeStrategy := {
  case PathList("module-info.class", _*) => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value(x)
}