scalaVersion     := "2.13.2"
version          := "0.1.0-SNAPSHOT"
organization     := "io.github.manuzhang"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "os-lib" % "0.7.0",
  "com.lihaoyi" %% "scalatags" % "0.8.2",
  "com.vladsch.flexmark" % "flexmark-all" % "0.61.28",
  "org.rogach" %% "scallop" % "3.5.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

