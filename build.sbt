name := "covid-telegram"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies += "com.github.pengrad" % "java-telegram-bot-api" % "5.1.0"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.7"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.14"
libraryDependencies += "org.jfree" % "jfreechart" % "1.5.3"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.8" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions := Seq("-unchecked", "-deprecation")

assembly / assemblyMergeStrategy := {
  case "META-INF/versions/9/module-info.class" => MergeStrategy.concat
  case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
  case x => (assembly / assemblyMergeStrategy).value(x)
}